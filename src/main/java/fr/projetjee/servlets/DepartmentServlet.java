package fr.projetjee.servlets;

import fr.projetjee.dao.DepartmentDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.Action;
import fr.projetjee.enums.Role;
import fr.projetjee.model.Department;
import fr.projetjee.model.User;
import fr.projetjee.security.RolePermissions;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * DepartmentServlet handles CRUD operations and filtering for departments.
 *
 * <p>
 * Security:
 * - CSRF protection: each POST request must include a valid CSRF token generated in the session during GET requests.
 * - Role-based access control:
 *       administrators and department heads have full access;
 *       RH department employees have full access to all departments.
 *
 * <p>
 * Supported actions (via POST 'action' parameter):
 * - register : Create a new department
 * - update   : Update an existing department
 * - delete   : Delete a department
 * - filter   : Apply filters to department list
 * - reset    : Reset filters
 * - assign   : Assign user to department
 *
 * <p>
 * Filters (name, code) are stored in session and applied to GET requests.
 */
@WebServlet("/departments")
public class DepartmentServlet extends HttpServlet {

    private DepartmentDAO departmentDAO;
    private UserDAO userDAO;

    /**
     * Initializes the servlet by creating DAO instances.
     * @throws ServletException if DAOs cannot be initialized
     */
    @Override
    public void init() throws ServletException {
        try {
            departmentDAO = new DepartmentDAO();
            userDAO = new UserDAO();
            System.out.println("[INFO][Servlet] DepartmentServlet initialisé avec succès.");
        } catch (Exception e) {
            System.err.println("[ERROR][Servlet] Erreur lors de l'initialisation de DepartmentServlet : " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Impossible d'initialiser les DAO : " + e.getMessage());
        }
    }

    /**
     * Cleans up resources when the servlet is destroyed.
     */
    @Override
    public void destroy() {
        System.out.println("[INFO][Servlet] DepartmentServlet détruit");
        super.destroy();
    }

    /**
     * Handles GET requests.
     * Retrieves departments according to the user's role and applied filters,
     * then forwards to departments.jsp.
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

        // Generate CSRF token if not present
        if (csrfToken == null) {
            csrfToken = new BigInteger(130, new SecureRandom()).toString(32);
            session.setAttribute("csrfToken", csrfToken);
        }

        // Retrieve filters from session
        String nameFilter = (String) session.getAttribute("filter_name");
        String codeFilter = (String) session.getAttribute("filter_code");

        // Load departments based on user role and filters
        List<Department> departments = loadDepartmentsForUser(user, nameFilter, codeFilter);

        request.setAttribute("departments", departments);
        request.setAttribute("employees", userDAO.findAll()); // For user assignment
        request.setAttribute("csrfToken", csrfToken);

        // Set filter attributes for JSP
        request.setAttribute("filter_name", nameFilter != null ? nameFilter : "");
        request.setAttribute("filter_code", codeFilter != null ? codeFilter : "");

        request.getRequestDispatcher("departments.jsp").forward(request, response);
    }

    /**
     * Handles POST requests.
     * Dispatches actions: register, update, delete, filter, reset, assign.
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

        // CSRF Protection
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

        // Special case: RH department employees have full department access
        boolean isRH = user.getRole() == Role.EMPLOYE && user.getDepartment() != null && "RH".equalsIgnoreCase(user.getDepartment().getCode());
        if (!isRH && requiredAction != null && !RolePermissions.hasPermission(user.getRole(), requiredAction)) {
            request.setAttribute("error", "Vous n'avez pas la permission pour cette action.");
            System.out.println("[SECURITY][Servlet] Permission refusée pour " + user.getFullName() + " sur l'action " + action);
            doGet(request, response);
            return;
        }

        try {
            switch (action) {
                case "register" -> addDepartment(request, response);
                case "update" -> updateDepartment(request, response);
                case "delete" -> deleteDepartment(request, response);
                case "assign" -> assignUserToDepartment(request, response);
                case "filter" -> applyFilters(request, response);
                case "reset" -> resetFilters(request, response);
                default -> {
                    request.setAttribute("error", "Action invalide.");
                    System.out.println("[ERROR][Servlet] Action invalide reçue: " + action);
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
     * Creates a new department.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if validation or forwarding fails
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    private void addDepartment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = safeTrim(request.getParameter("name"));
        String code = safeTrim(request.getParameter("code"));
        String description = safeTrim(request.getParameter("description"));

        // Validation
        if (name == null || name.isEmpty() || name.length() > 100) {
            request.setAttribute("error", "Nom du département invalide (1-100 caractères)");
            System.out.println("[ERROR][Servlet] Nom du département manquant ou invalide lors de l'ajout");
            doGet(request, response);
            return;
        }

        if (code == null || code.isEmpty() || code.length() > 10) {
            request.setAttribute("error", "Code du département invalide (1-10 caractères)");
            System.out.println("[ERROR][Servlet] Code du département manquant ou invalide lors de l'ajout");
            doGet(request, response);
            return;
        }

        // Check for duplicate code
        Optional<Department> existingDept = departmentDAO.findByName(name);
        if (existingDept.isPresent()) {
            request.setAttribute("error", "Un département avec ce nom existe déjà");
            System.out.println("[ERROR][Servlet] Département avec ce nom existe déjà: " + name);
            doGet(request, response);
            return;
        }

        Department department = new Department(name, code, description != null ? description : "");
        Department saved = departmentDAO.save(department);

        if (saved != null) {
            System.out.println("[SUCCESS][Servlet] Département ajouté : " + department.getName() + ", code : " + department.getCode());
            response.sendRedirect("departments");
        } else {
            request.setAttribute("error", "Erreur lors de l'ajout du département.");
            System.out.println("[ERROR][Servlet] Échec de l'ajout du département : " + name);
            doGet(request, response);
        }
    }

    /**
     * Updates department details.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if department validation or forwarding fails
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    private void updateDepartment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer id = Integer.parseInt(safeTrim(request.getParameter("id")));
        String name = safeTrim(request.getParameter("name"));
        String code = safeTrim(request.getParameter("code"));
        String description = safeTrim(request.getParameter("description"));

        Department department = departmentDAO.findById(id).orElse(null);
        if (department == null) {
            System.out.println("[ERROR][Servlet] Département introuvable pour l'ID : " + id);
            request.setAttribute("error", "Le département est introuvable.");
            doGet(request, response);
            return;
        }

        // Validation
        if (name == null || name.isEmpty() || name.length() > 100) {
            request.setAttribute("error", "Nom du département invalide (1-100 caractères)");
            doGet(request, response);
            return;
        }

        if (code == null || code.isEmpty() || code.length() > 10) {
            request.setAttribute("error", "Code du département invalide (1-10 caractères)");
            doGet(request, response);
            return;
        }

        // Check for duplicate code (excluding current department)
        Optional<Department> existingDept = departmentDAO.findByName(name);
        if (existingDept.isPresent() && !existingDept.get().getId().equals(id)) {
            request.setAttribute("error", "Un département avec ce nom existe déjà");
            doGet(request, response);
            return;
        }

        department.setName(name);
        department.setCode(code);
        if (description != null) {
            department.setDescription(description);
        }

        Department updated = departmentDAO.update(department);
        if (updated != null) {
            System.out.println("[SUCCESS][Servlet] Département mis à jour : " + department.getName() + ", code : " + department.getCode());
            response.sendRedirect("departments");
        } else {
            request.setAttribute("error", "Erreur lors de la mise à jour du département.");
            System.out.println("[ERROR][Servlet] Échec mise à jour département, ID=" + id);
            doGet(request, response);
        }
    }

    /**
     * Deletes a department.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if department validation or forwarding fails
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    private void deleteDepartment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer id = Integer.parseInt(request.getParameter("id"));
        Department department = departmentDAO.findById(id).orElse(null);

        if (department == null) {
            request.setAttribute("error", "Département introuvable.");
            doGet(request, response);
            return;
        }

        // Check if department has users
        long userCount = departmentDAO.countUsersByDepartment(id);
        if (userCount > 0) {
            request.setAttribute("error", "Impossible de supprimer le département : il contient encore des employés.");
            System.out.println("[ERROR][Servlet] Tentative de suppression d'un département avec des employés, ID=" + id);
            doGet(request, response);
            return;
        }

        boolean success = departmentDAO.deleteById(id);
        if (success) {
            System.out.println("[SUCCESS][Servlet] Département supprimé, ID : " + id);
        } else {
            System.out.println("[ERROR][Servlet] Échec suppression département, ID : " + id);
        }
        response.sendRedirect("departments");
    }

    /**
     * Assigns a user to a department.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if validation fails
     * @throws IOException if an I/O error occurs during redirect
     */
    private void assignUserToDepartment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer departmentId = Integer.parseInt(request.getParameter("departmentId"));
        String registrationNumber = safeTrim(request.getParameter("registrationNumber"));

        if (registrationNumber == null || registrationNumber.isEmpty()) {
            request.setAttribute("error", "Matricule de l'employé requis");
            doGet(request, response);
            return;
        }

        boolean success = departmentDAO.assignUserToDepartment(departmentId, registrationNumber);
        if (success) {
            System.out.println("[SUCCESS][Servlet] Utilisateur " + registrationNumber + " assigné au département ID=" + departmentId);
            response.sendRedirect("departments");
        } else {
            request.setAttribute("error", "Erreur lors de l'assignation de l'employé au département.");
            System.out.println("[ERROR][Servlet] Échec assignation utilisateur au département");
            doGet(request, response);
        }
    }

    /**
     * Stores department list filters (name, code) in session.
     * Redirects to GET view to apply filters.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException if an I/O error occurs during redirect
     */
    private void applyFilters(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = safeTrim(request.getParameter("name"));
        String code = safeTrim(request.getParameter("code"));

        HttpSession session = request.getSession();
        session.setAttribute("filter_name", name);
        session.setAttribute("filter_code", code);

        response.sendRedirect("departments");
    }

    /**
     * Clears department list filters from session.
     * Redirects to GET view to show all departments.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException if an I/O error occurs during redirect
     */
    private void resetFilters(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("filter_name");
        session.removeAttribute("filter_code");
        response.sendRedirect("departments");
    }

    /**
     * Returns a list of departments accessible by the user,
     * applying role-based restrictions and session filters.
     *
     * @param user Current user
     * @param nameFilter Filter for department name
     * @param codeFilter Filter for department code
     * @return List of departments
     */
    private List<Department> loadDepartmentsForUser(User user, String nameFilter, String codeFilter) {
        // Access denied if no READ_DEPARTMENT permission
        if (!RolePermissions.hasPermission(user.getRole(), Action.READ_DEPARTMENT)) {
            return Collections.emptyList();
        }

        // Special handling for employees in RH department
        boolean isRHDepartment = user.getDepartment() != null &&
                "RH".equalsIgnoreCase(user.getDepartment().getCode());

        switch (user.getRole()) {
            case ADMINISTRATEUR, CHEF_DEPARTEMENT -> {
                // Complete access
                return getFilteredOrAllDepartments(nameFilter, codeFilter);
            }
            case EMPLOYE, CHEF_PROJET -> {
                // Complete access for RH department employees
                if (isRHDepartment) {
                    return getFilteredOrAllDepartments(nameFilter, codeFilter);
                }
                // Regular employees can only see their own department
                if (user.getDepartment() != null) {
                    return List.of(user.getDepartment());
                }
                return Collections.emptyList();
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Returns all departments or applies filters if specified.
     *
     * @param nameFilter Filter for department name
     * @param codeFilter Filter for department code
     * @return Filtered or all departments
     */
    private List<Department> getFilteredOrAllDepartments(String nameFilter, String codeFilter) {
        boolean hasFilters = (nameFilter != null && !nameFilter.isEmpty()) ||
                (codeFilter != null && !codeFilter.isEmpty());

        if (hasFilters) {
            // Since DepartmentDAO doesn't have a filter method, we'll filter in memory
            List<Department> allDepartments = departmentDAO.findAll();
            return allDepartments.stream()
                    .filter(dept ->
                            (nameFilter == null || nameFilter.isEmpty() ||
                                    dept.getName().toLowerCase().contains(nameFilter.toLowerCase())) &&
                                    (codeFilter == null || codeFilter.isEmpty() ||
                                            dept.getCode().toLowerCase().contains(codeFilter.toLowerCase()))
                    )
                    .toList();
        } else {
            return departmentDAO.findAll();
        }
    }

    /**
     * Maps a string action parameter to an Action enum.
     *
     * @param action String action
     * @return Action enum or null if invalid
     */
    private Action mapActionString(String action) {
        return switch (action) {
            case "register" -> Action.CREATE_DEPARTMENT;
            case "update" -> Action.UPDATE_DEPARTMENT;
            case "delete" -> Action.DELETE_DEPARTMENT;
            case "assign" -> Action.UPDATE_DEPARTMENT; // Assigning is considered an update
            case "filter", "reset" -> Action.FILTER_DEPARTMENT;
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