package fr.projetjee.servlets;

import fr.projetjee.enums.Action;
import fr.projetjee.enums.Role;
import fr.projetjee.enums.Status;
import fr.projetjee.security.RolePermissions;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.security.SecureRandom;
import java.math.BigInteger;
import fr.projetjee.model.*;
import fr.projetjee.dao.*;


/**
 * ProjectServlet handles CRUD operations and filtering for projects.
 *
 * <p>
 * Security:
 * - CSRF protection: each POST request must include a valid CSRF token generated in the session during GET requests.
 * - Role-based access control:
 *       administrators and department heads have full access;
 *       project managers can update/delete only their own projects;
 *       employees can only see their own projects unless in the HR department.
 *
 * <p>
 * Supported actions (via POST 'action' parameter):
 * - register : Create a new project
 * - update   : Update an existing project
 * - delete   : Delete a project
 * - filter   : Apply filters to project list
 * - reset    : Reset filters
 * <p>
 *
 * Filters (name, manager, status) are stored in session and applied to GET requests.
 */
@WebServlet("/projects")
public class ProjectServlet extends HttpServlet {

    private ProjectDAO projectDAO;
    private UserDAO userDAO;

    /**
     * Initializes the servlet by creating DAO instances.
     * @throws ServletException if DAOs cannot be initialized
     */
    @Override
    public void init() throws ServletException {
        try {
            projectDAO = new ProjectDAO();
            userDAO = new UserDAO();
            System.out.println("[INFO][Servlet] ProjectServlet initialisé avec succès.");
        } catch (Exception e) {
            System.err.println("[ERROR][Servlet] Erreur lors de l'initialisation de ProjectServlet : " + e.getMessage());
            e.printStackTrace();
            // Optional : rethrow as ServletException to prevent servlet from starting
            throw new ServletException("Impossible d'initialiser les DAO : " + e.getMessage());
        }
    }

    /**
     * Cleans up resources when the servlet is destroyed.
     */
    @Override
    public void destroy() {
        System.out.println("[INFO][Servlet] ProjectServlet détruit");
        super.destroy();
    }

    /**
     * Handles GET requests.
     * Retrieves projects according to the user's role and applied filters,
     * then forwards to projects.jsp.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if forwarding to JSP fails
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        String csrfToken = (String) session.getAttribute("csrfToken");

        // Security check: redirect if no user in session
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Retrieve filters from session
        String nameFilter = (String) session.getAttribute("filter_name");
        String managerFilter = (String) session.getAttribute("filter_manager");
        Status statusFilter = (Status) session.getAttribute("filter_status");

        // Load projects based on user role and filters
        List<Project> projects = loadProjectsForUser(user, nameFilter, managerFilter, statusFilter);

        // For datalist in JSP
        List<Project> allProjects = projectDAO.findAll();

        request.setAttribute("allProjects", allProjects);
        request.setAttribute("projects", projects);
        request.setAttribute("chefs", userDAO.findByRole(Role.CHEF_PROJET));

        // Set filter attributes for JSP
        request.setAttribute("filter_name", nameFilter != null ? nameFilter : "");
        request.setAttribute("filter_manager", managerFilter != null ? managerFilter : "");
        request.setAttribute("filter_status", statusFilter != null ? statusFilter.name() : "");

        if (csrfToken == null) {
            csrfToken = new BigInteger(130, new SecureRandom()).toString(32); // Generate a secure and random CSRF token
            session.setAttribute("csrfToken", csrfToken);
        }
        request.setAttribute("csrfToken", csrfToken);

        request.getRequestDispatcher("projects.jsp").forward(request, response);
    }

    /**
     * Handles POST requests.
     * Dispatches actions: register, update, delete, filter, reset.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if an error occurs during processing or forwarding
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        String action = safeTrim(request.getParameter("action"));
        String sessionToken = (String) session.getAttribute("csrfToken");
        String requestToken = request.getParameter("csrfToken");

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // CSRF Protection , verify that the CSRF token in the request matches the one in the session
        if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {
            System.out.println("[SECURITY][Servlet] CSRF token invalide pour l'utilisateur : " + user.getFullName());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalide");
            return;
        }

        if (action == null) {
            request.setAttribute("error", "Action manquante");
            System.out.println("[ERROR][Servlet] Action manquante dans la requête POST");
            doGet(request, response);
            return;
        }

        // Mapping string action → enum Action
        Action requiredAction = mapActionString(action);

        // Special case: RH department employees have full project access
        boolean isRH = user.getRole() == Role.EMPLOYE && user.getDepartment() != null && "RH".equalsIgnoreCase(user.getDepartment().getCode());
        if (!isRH && requiredAction != null && !RolePermissions.hasPermission(user.getRole(), requiredAction)) {
            request.setAttribute("error", "Vous n'avez pas la permission pour cette action.");
            System.out.println("[SECURITY][Servlet] Permission refusée pour " + user.getFullName() + " sur l'action " + action);
            doGet(request, response);
            return;
        }

        try {
            switch (action) {
                case "register" -> addProject(request, response);
                case "update" -> updateProject(request, response);
                case "delete" -> deleteProject(request, response);
                case "filter" -> applyFilters(request, response);
                case "reset" -> resetFilters(request, response);
                default -> {
                    request.setAttribute("error", "Action invalide.");
                    System.out.println("[ERROR][Servlet]  Action invalide reçue: " + action);
                    doGet(request, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur serveur: " + e.getMessage());
            doGet(request, response);
        }
    }

    /**
     * Creates a new project and assigns the project manager.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if validation or forwarding fails
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    private void addProject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = safeTrim(request.getParameter("nom"));
        String managerMatricule = safeTrim(request.getParameter("chefProjet"));
        String statusParam = request.getParameter("statut");
        String description = ""; // Optional description can be added later

        if (name == null || name.isEmpty() || name.length() > 100) {
            request.setAttribute("error", "Nom du projet invalide ");
            System.out.println("[ERROR][Servlet] Nom du projet manquant lors de l'ajout");
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

            projectDAO.assignUserToProject(project.getId(),managerMatricule);
            System.out.println("[SUCCESS][Servlet] Chef de projet assigné au projet.");
            response.sendRedirect("projects");
        } else {
            request.setAttribute("error", "Erreur lors de l'ajout du projet.");
            System.out.println("[ERROR][Servlet] Échec de l'ajout du projet : " + name + ", chef : " + manager.getFullName());
            doGet(request, response);
        }

    }

    /**
     * Updates project details including name, description, status, and project manager.
     * Only allows project managers to update their own projects.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if project validation or forwarding fails
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    private void updateProject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer id = Integer.parseInt(safeTrim(request.getParameter("id")));
        String name = safeTrim(request.getParameter("nom"));
        String managerMatricule = safeTrim(request.getParameter("chefProjet"));
        String statusParam = request.getParameter("statut");
        String description = safeTrim(request.getParameter("description"));

        Project project = projectDAO.findById(id).orElse(null);
        if (project == null) {
            System.out.println("[ERROR][Servlet] Projet introuvable pour l'ID : " + id); request.setAttribute("error","Le projet est introuvable.");
            doGet(request,response); return; }

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");

        // Security: Chefs de projet can only update their own projects
        if (user.getRole() == Role.CHEF_PROJET) { checkProjectManagerPermission(request, response, user, project);}

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

    /**
     * Deletes a project.
     * Only allows project managers to delete their own projects.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if project validation or forwarding fails
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    private void deleteProject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer id = Integer.parseInt(request.getParameter("id"));
        Project project = projectDAO.findById(id).orElse(null);
        if (project == null) {
            request.setAttribute("error", "Projet introuvable.");
            doGet(request, response);
            return;
        }
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");

        if (user.getRole() == Role.CHEF_PROJET) { checkProjectManagerPermission(request, response, user, project);}
        projectDAO.deleteById(id);
        System.out.println("[INFO][Servlet] Projet supprimé, ID : " + id);
        response.sendRedirect("projects");
    }

    /**
     * Retrieves the project manager by matricule and returns an error if invalid.
     * If the project manager is invalid, sets an error and return null, otherwise returns the User object.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param matricule String employee number of the manager
     * @return User object if valid, null otherwise
     * @throws ServletException if validation or forwarding fails
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    private User getProjectManagerOrSendError(HttpServletRequest request, HttpServletResponse response, String matricule) throws ServletException, IOException {
        User manager = userDAO.findByMatricule(matricule).orElse(null);
        if (manager == null || !userDAO.isUserProjectManager(Role.CHEF_PROJET, manager.getId())) {
            System.out.println("[ERROR][Servlet] Chef de projet invalide: " + matricule + ", trouvé: " + (manager != null ? manager.getFullName() : "null") + ", rôle: " + (manager != null ? manager.getRole() : "null"));
            request.setAttribute("error", "Le chef de projet spécifié est invalide ou n'est pas un chef de projet.");
            doGet(request, response);
            return null; // we return null to indicate failure
        }
        System.out.println("[INFO][Servlet] Manager valide trouvé : " + manager.getFullName() + ", matricule : " + matricule);
        return manager;
    }

    /**
     * Checks if the current user is the manager of the given project.
     * If the user is not the project manager, sets an error attribute and forwards
     * the request to the "index.jsp" page, then throws a ServletException to stop further processing.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @param user     the current user
     * @param project  the project to check against
     * @throws ServletException if the user is not the project manager or if forwarding fails
     * @throws IOException      if an I/O error occurs during request forwarding
     */
    private void checkProjectManagerPermission(HttpServletRequest request, HttpServletResponse response, User user, Project project) throws ServletException, IOException {
        if (!project.getProjectManager().getId().equals(user.getId())) {
            System.out.println("[SECURITY][Servlet] Permission refusée : " + user.getFullName() + " n'est pas le chef du projet " + project.getName());
            request.setAttribute("error", "Vous n'êtes pas le chef de ce projet.");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            throw new ServletException("Permission denied");
        }
    }

    /**
     * Stores project list filters (name, manager, status) in session.
     * Redirects to GET view to apply filters.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException if an I/O error occurs during redirect
     */
    private void applyFilters(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        // GET Redirection -> avoids "form resubmission" on reload
        response.sendRedirect("projects");
    }

    /**
     * Clears project list filters from session.
     * Redirects to GET view to show all projects.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException if an I/O error occurs during redirect
     */
    private void resetFilters(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("filter_name");
        session.removeAttribute("filter_manager");
        session.removeAttribute("filter_status");
        response.sendRedirect("projects");
    }

    /**
     * Returns a list of projects accessible by the user,
     * applying role-based restrictions and session filters.
     *
     * @param user Current user
     * @param nameFilter Filter for project name
     * @param managerFilter Filter for project manager
     * @param statusFilter Filter for project status
     * @return List of projects
     */
    private List<Project> loadProjectsForUser(User user, String nameFilter, String managerFilter, Status statusFilter) {

        // Access denied if no READ_PROJECT permission
        if (!RolePermissions.hasPermission(user.getRole(), Action.READ_PROJECT)) {
            return Collections.emptyList();
        }

        // Special handling for employees in RH department
        boolean isRHDepartment = user.getDepartment() != null &&
                "RH".equalsIgnoreCase(user.getDepartment().getCode());

        switch (user.getRole()) {
            case ADMINISTRATEUR, CHEF_DEPARTEMENT -> {
                // Complete access
                return getFilteredOrAllProjects(nameFilter, managerFilter, statusFilter);
            }
            case EMPLOYE, CHEF_PROJET -> {
                // Complete access for RH department employees
                if (isRHDepartment) {
                    return getFilteredOrAllProjects(nameFilter, managerFilter, statusFilter);
                }
                return projectDAO.findByUserId(user.getId());
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Returns all projects or applies filters if specified.
     *
     * @param nameFilter Filter for project name
     * @param managerFilter Filter for project manager
     * @param statusFilter Filter for project status
     * @return Filtered or all projects
     */
    private List<Project> getFilteredOrAllProjects(String nameFilter, String managerFilter, Status statusFilter) {
        boolean hasFilters =
                (nameFilter != null && !nameFilter.isEmpty()) ||
                        (managerFilter != null && !managerFilter.isEmpty()) ||
                        statusFilter != null;

        return hasFilters
                ? projectDAO.findProjectsWithFilters(nameFilter, managerFilter, statusFilter)
                : projectDAO.findAll();
    }

    /**
     * Maps a string action parameter to an Action enum.
     *
     * @param action String action
     * @return Action enum or null if invalid
     */
    private Action mapActionString(String action) {
        return switch (action) {
            case "register" -> Action.CREATE_PROJECT;
            case "update" -> Action.UPDATE_PROJECT;
            case "delete" -> Action.DELETE_PROJECT;
            case "filter", "reset" -> Action.FILTER_PROJECT;
            default -> null;
        };
    }

    /**
     * Safely trims a string, returning null if the input is null.
     *
     * @param value Input string
     * @return Trimmed string or null
     */
    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }


}
