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
    public void init() throws ServletException {
        try {
            projectDAO = new ProjectDAO();
            userDAO = new UserDAO();
            System.out.println("[INFO] ProjectServlet initialisé avec succès.");
        } catch (Exception e) {
            System.err.println("[ERROR] Erreur lors de l'initialisation de ProjectServlet : " + e.getMessage());
            e.printStackTrace();
            // Optionnel : lancer une exception pour empêcher le servlet de démarrer
            throw new ServletException("Impossible d'initialiser les DAO : " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = safeTrim(request.getParameter("action"));

        if (action == null) {
            request.setAttribute("error", "Action manquante");
            System.out.println("[ERROR] Action manquante dans la requête POST avec action = " + action);
            doGet(request, response);
            return;
        }

        if ("filter".equals(action)) {
            FilterPost(request, response);
            return;
        }
        if ("reset".equals(action)) {
            resetFilters(request, response);
            return;
        }
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
        String name = safeTrim(request.getParameter("nom"));
        String managerMatricule = safeTrim(request.getParameter("chefProjet"));
        String statusParam = request.getParameter("statut");
        String description = ""; // Optional description can be added later

        if (name == null || name.isEmpty() || name.length() > 100) {
            request.setAttribute("error", "Nom du projet invalide ");
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
        String name = safeTrim(request.getParameter("nom"));
        String managerMatricule = safeTrim(request.getParameter("chefProjet"));
        String statusParam = request.getParameter("statut");
        String description = safeTrim(request.getParameter("description"));

        Project project = projectDAO.findById(id).orElse(null);
        if (project == null) {
            System.out.println("[ERROR][Servlet] Projet introuvable pour l'ID : " + id); request.setAttribute("error","Le projet est introuvable.");
            doGet(request,response); return; }

        User updatedManager = getProjectManagerOrSendError(request, response, managerMatricule);
        if (updatedManager == null) return; // stop if invalid

        if(name.length() <= 100) { project.setName(name); }
        if (description != null && !description.isEmpty() && description.length() <= 500) { project.setDescription(description); }

        // Update project manager if changed
        boolean updated = projectDAO.updateProjectManager(project.getId(), updatedManager.getMatricule());
        if (!updated) {
            request.setAttribute("error", "Impossible de mettre à jour le chef de projet.");
            doGet(request, response);
            return;
        }


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
        projectDAO.deleteById(id);
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
        HttpSession session = request.getSession();

        String nameFilter = (String) session.getAttribute("filter_name");
        String managerFilter = (String) session.getAttribute("filter_manager");
        Status statusFilter = (Status) session.getAttribute("filter_status");

        List<Project> allProjects = projectDAO.findAll(); // Pour la datalist complète
        List<Project> filteredProjects;
        // Appliquer les filtres uniquement si au moins un champ est rempli
        if ((nameFilter != null && !nameFilter.isEmpty()) ||
                (managerFilter != null && !managerFilter.isEmpty()) ||
                statusFilter != null) {

            filteredProjects = projectDAO.findProjectsWithFilters(nameFilter, managerFilter, statusFilter);

        } else {
            // Aucun filtre => récupérer tous les projets
            filteredProjects = allProjects;
        }

        request.setAttribute("allProjects", allProjects);
        request.setAttribute("projects", filteredProjects);
        request.setAttribute("chefs", userDAO.findByRole(Role.CHEF_PROJET));

        // Pré-remplir les filtres pour le JSP
        request.setAttribute("filter_name", nameFilter != null ? nameFilter : "");
        request.setAttribute("filter_manager", managerFilter != null ? managerFilter : "");
        request.setAttribute("filter_status", statusFilter != null ? statusFilter.name() : "");

        request.getRequestDispatcher("projects.jsp").forward(request, response);
    }

    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    private void FilterPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = safeTrim(request.getParameter("name"));
        String manager = safeTrim(request.getParameter("manager"));
        String statusStr = safeTrim(request.getParameter("status"));

        Status status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Status.valueOf(statusStr);
            } catch (IllegalArgumentException ignored) { }
        }

        HttpSession session = request.getSession();
        session.setAttribute("filter_name", name);
        session.setAttribute("filter_manager", manager);
        session.setAttribute("filter_status", status);

        // Redirection GET → évite le "rechargement du formulaire"
        response.sendRedirect("projects");
    }
    private void resetFilters(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("filter_name");
        session.removeAttribute("filter_manager");
        session.removeAttribute("filter_status");
        response.sendRedirect("projects");
    }
    @Override
    public void destroy() {
        System.out.println("ProjectServlet détruit");
        super.destroy();
    }
}
