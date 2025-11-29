package fr.projetjee.servlets;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import fr.projetjee.dao.ProjectDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.model.Project;
import fr.projetjee.model.User;
import fr.projetjee.enums.Role;
import fr.projetjee.security.RolePermissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static fr.projetjee.security.RolePermissions.userCanAccessProject;

@WebServlet("/project-detail/*")
public class ProjectDetailServlet extends HttpServlet {

    private final ProjectDAO projectDAO = new ProjectDAO();
    UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        System.out.println("User role: " + (user != null ? user.getRole() : "null"));

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Integer> tokens = (Map<String, Integer>) session.getAttribute("projectTokens");

        if (tokens == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès non autorisé.");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Projet introuvable");
            return;
        }

        String token = pathInfo.substring(1);
        if (!tokens.containsKey(token)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token invalide");
            return;
        }

        int projectId = tokens.get(token);

        Optional<Project> optProject = projectDAO.findByIdWithUsers(projectId);

        if (optProject.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Projet introuvable");
            return;
        }
        Project project = optProject.get();

        // Vérification des permissions d'accès
        if (!userCanAccessProject(user, project)) {
            System.out.println("[SECURITY] Tentative d'accès non autorisé : "
                    + user.getFullName() + " → projet " + project.getName());

            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Vous n'avez pas la permission d'accéder à ce projet.");
            return;
        }

        if ("image".equals(request.getParameter("action"))) {
            System.out.println("Appel");
            serveUserImage(request, response, project);
            return;
        }

        Set<User> usersAssigned = project.getUsers();
        usersAssigned.size(); // ← TRÈS IMPORTANT : initialise la collection lazy

        // Récupérer tous les employés disponibles (pas encore assignés au projet)
        // Exclure les RH et les Admin
        UserDAO userDAO = new UserDAO();
        List<User> allUsers = userDAO.findAll();
        List<User> availableEmployees = allUsers.stream()
                .filter(u -> !usersAssigned.contains(u))
                .filter(u -> RolePermissions.canBeAssignedToProject(u))
                .collect(Collectors.toList());

        // Vérifier si l'utilisateur peut gérer les membres du projet
        boolean canManageMembers = RolePermissions.canManageProjectMembers(user, project);
        // Vérifier si l'utilisateur peut voir la colonne matricule
        boolean canViewMatricule = RolePermissions.canViewMatriculeColumn(user);


        request.setAttribute("availableEmployees", availableEmployees);
        request.setAttribute("canManageMembers", canManageMembers);
        request.setAttribute("canViewMatricule", canViewMatricule);
        request.setAttribute("usersAssigned", usersAssigned);
        request.setAttribute("project", project);
        request.getRequestDispatcher("/projectdetails.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String action = request.getParameter("action");
        String projectIdStr = request.getParameter("projectId");

        if (projectIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID projet manquant");
            return;
        }

        int projectId = Integer.parseInt(projectIdStr);
        Optional<Project> optProject = projectDAO.findByIdWithUsers(projectId);

        if (optProject.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Projet introuvable");
            return;
        }

        Project project = optProject.get();

        // Vérification des permissions
        if (!userCanAccessProject(user, project)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès non autorisé");
            return;
        }

        // Vérification des permissions de gestion des membres
        if (!RolePermissions.canManageProjectMembers(user, project)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Vous n'avez pas la permission de gérer les membres de ce projet");
            return;
        }

        if ("assign".equals(action)) {
            String singleEmployee = request.getParameter("singleEmployee");
            String multipleEmployees = request.getParameter("multipleEmployees");

            boolean success = false;

            if (multipleEmployees != null && !multipleEmployees.trim().isEmpty()) {
                // Mode multiple - Valider que tous les employés peuvent être assignés
                String[] matricules = multipleEmployees.split(",");
                int successCount = 0;

                for (String matricule : matricules) {
                    matricule = matricule.trim();
                    if (!matricule.isEmpty()) {
                        // Vérifier que l'employé peut être assigné (pas RH, pas Admin)
                        Optional<User> userToAssign = userDAO.findByMatricule(matricule);
                        if (userToAssign.isPresent() &&
                                RolePermissions.canBeAssignedToProject(userToAssign.get())) {

                            if (projectDAO.assignUserToProject(projectId, matricule)) {
                                successCount++;
                            }
                        } else {
                            System.err.println("[SECURITY] Tentative d'assignation d'un utilisateur RH/Admin : " + matricule);
                        }
                    }
                }

                success = successCount > 0;
                if (success) {
                    session.setAttribute("message", successCount + " employé(s) assigné(s) avec succès");
                } else {
                    session.setAttribute("error", "Aucun employé valide n'a pu être assigné");
                }
            } else if (singleEmployee != null && !singleEmployee.trim().isEmpty()) {
                // Mode simple - Valider que l'employé peut être assigné
                Optional<User> userToAssign = userDAO.findByMatricule(singleEmployee.trim());
                if (userToAssign.isPresent() &&
                        RolePermissions.canBeAssignedToProject(userToAssign.get())) {

                    success = projectDAO.assignUserToProject(projectId, singleEmployee.trim());
                    if (success) {
                        session.setAttribute("message", "Employé assigné avec succès");
                    } else {
                        session.setAttribute("error", "Erreur lors de l'assignation");
                    }
                } else {
                    session.setAttribute("error", "Cet employé ne peut pas être assigné au projet");
                }
            }

        } else if ("remove".equals(action)) {
            String userMatricule = request.getParameter("userMatricule");

            if (userMatricule != null && !userMatricule.trim().isEmpty()) {
                // Vérifier qu'on ne retire pas le chef de projet
                if (project.getProjectManager() != null &&
                        project.getProjectManager().getMatricule().equals(userMatricule)) {
                    session.setAttribute("error", "Impossible de retirer le chef de projet");
                } else {
                    boolean success = projectDAO.removeUserFromProject(projectId, userMatricule);

                    if (success) {
                        session.setAttribute("message", "Employé retiré avec succès");
                    } else {
                        session.setAttribute("error", "Erreur lors du retrait de l'employé");
                    }
                }
            }
        }

        // Redirection vers la même page
        Map<String, Integer> tokens = (Map<String, Integer>) session.getAttribute("projectTokens");
        String token = tokens.entrySet().stream()
                .filter(e -> e.getValue() == projectId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (token != null) {
            response.sendRedirect(request.getContextPath() + "/project-detail/" + token);
        } else {
            response.sendRedirect(request.getContextPath() + "/projects");
        }
    }
    private void serveUserImage(HttpServletRequest request, HttpServletResponse response, Project project)
            throws IOException {

        try {
            User user = project.getProjectManager();

            if (user !=null && user.getImage() != null) {
                byte[] img = user.getImage();

                response.setContentType("image/jpeg");
                response.setContentLength(img.length);
                response.getOutputStream().write(img);
                response.getOutputStream().flush();
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
