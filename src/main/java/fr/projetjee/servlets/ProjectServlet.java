package fr.projetjee.servlets;

import fr.projetjee.enums.Role;
import fr.projetjee.enums.Status;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import fr.projetjee.model.*;
import fr.projetjee.dao.*;

@WebServlet("/projects")
public class ProjectServlet extends HttpServlet {

    private ProjectDAO projectDAO;
    private UserDAO userDAO;

    @Override
    public void init() {
        projectDAO = new ProjectDAO();
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            switch (action) {
                case "register":
                    addProject(request, response);
                    break;

                case "update":
                    updateProject(request, response);
                    break;

                case "delete":
                    deleteProject(request, response);
                    break;

                default:
                    request.setAttribute("error", "Action invalide.");
                    System.out.println("[ERROR] Action invalide reçue: " + action);
                    doGet(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur serveur: " + e.getMessage());
            doGet(request, response);
        }
    }

    private void addProject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("nom");
        String managerMatricule = request.getParameter("chefProjet");
        String statusParam = request.getParameter("statut");
        String description = ""; // Optional description can be added later

        if (name == null || name.isEmpty()) {
            request.setAttribute("error", "Le nom du projet est obligatoire.");
            System.out.println("[ERROR] Nom du projet manquant lors de l'ajout");
            doGet(request, response);
            return;
        }

        Status status;
        try {
            status = Status.valueOf(statusParam);
        } catch (Exception e) {
            status = Status.IN_PROGRESS;
        }

        User manager = getProjectManagerOrSendError(request, response, managerMatricule);
        if (manager == null) return; // stop if invalid

        Project project = new Project(name, manager, description, status);
        Project saved = projectDAO.save(project);
        if (saved != null) {
            System.out.println("[SUCCESS][Servlet] Projet ajouté : " + project.getName() + ", chef : " + manager.getFullName());
            response.sendRedirect("projects");
        } else {
            request.setAttribute("error", "Erreur lors de l'ajout du projet.");
            System.out.println("[ERROR][Servlet] Échec de l'ajout du projet : " + name + ", chef : " + manager.getFullName());
            doGet(request, response);
        }
        projectDAO.assignUserToProject(project.getId(),managerMatricule);
        System.out.println("[SUCCESS][Servlet] Chef de projet assigné au projet.");
    }

    private void updateProject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("nom");
        String managerMatricule = request.getParameter("chefProjet");
        String statusParam = request.getParameter("statut");
        String description = request.getParameter("description");

        Project project = projectDAO.findById(id).orElse(null);
        if (project == null) {
            System.out.println("[ERROR][Servlet] Projet introuvable pour l'ID : " + id); request.setAttribute("error","Le projet est introuvable.");
            doGet(request,response); return; }

        User updatedManager = getProjectManagerOrSendError(request, response, managerMatricule);
        if (updatedManager == null) return; // stop if invalid

        project.setName(name);
        if (description != null && !description.isEmpty()) { project.setDescription(description); }

        // Save the current manager before updating
        User currentManager = project.getProjectManager();
        if (currentManager == null || !currentManager.getMatricule().equals(managerMatricule)) {
            if (currentManager != null) {
                projectDAO.removeUserFromProject(project.getId(), currentManager.getMatricule());
                System.out.println("[INFO][Servlet] Ancien chef de projet désassigné : " + currentManager.getFullName());
            }

            projectDAO.assignUserToProject(project.getId(), updatedManager.getMatricule());
            System.out.println("[SUCCESS][Servlet] Nouveau chef de projet assigné : " + updatedManager.getFullName());
        }
        project.setProjectManager(updatedManager);

        try {
            project.setStatus(Status.valueOf(statusParam));
        } catch (Exception e) {
            project.setStatus(Status.IN_PROGRESS);
        }

        projectDAO.update(project);
        System.out.println("[SUCCESS][Servlet] Projet mis à jour : " + project.getName() + ", chef : " + updatedManager.getFullName());
        response.sendRedirect("projects");
    }

    private void deleteProject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            request.setAttribute("error", "ID du projet manquant.");
            System.out.println("[ERROR][Servlet] ID du projet manquant lors de la suppression");
            doGet(request, response);
            return;
        }

        Integer id = Integer.parseInt(idParam);
        projectDAO.delete(id);
        System.out.println("[INFO][Servlet] Projet supprimé, ID : " + id);
        response.sendRedirect("projects");
    }

    /** Retrieves the project manager by employee number and returns an error if invalid.
     * Returns null if the project manager is invalid, otherwise returns the User object.
     */
    private User getProjectManagerOrSendError(HttpServletRequest request, HttpServletResponse response, String matricule) throws ServletException, IOException {
        User manager = userDAO.findByMatricule(matricule).orElse(null);
        if (manager == null || !userDAO.isUserProjectManager(Role.CHEF_PROJET, manager.getId())) {
            System.out.println("[ERROR][Servlet] Chef de projet invalide: " + matricule + ", trouvé: " + (manager != null ? manager.getFullName() : "null") + ", rôle: " + (manager != null ? manager.getRole() : "null"));
            request.setAttribute("error", "Le chef de projet spécifié est invalide ou n'est pas un chef de projet.");
            doGet(request, response);
            return null; // on retournera null pour signaler que la suite ne doit pas continuer
        }
        System.out.println("[INFO][Servlet] Manager valide trouvé : " + manager.getFullName() + ", matricule : " + matricule);
        return manager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Project> projects = projectDAO.findAll();
        request.setAttribute("projects", projects);

        List<User> chefs = userDAO.findByRole(Role.CHEF_PROJET);
        request.setAttribute("chefs", chefs);
        request.getRequestDispatcher("projects.jsp").forward(request, response);
    }
}
