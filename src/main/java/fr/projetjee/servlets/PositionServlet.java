package fr.projetjee.servlets;

import fr.projetjee.dao.PositionDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.Action;
import fr.projetjee.enums.Role;
import fr.projetjee.model.Position;
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

/**
 * PositionServlet handles CRUD operations for positions.
 *
 * <p>
 * Security:
 * - CSRF protection: each POST request must include a valid CSRF token generated in the session during GET requests.
 * - Role-based access control:
 *      - Administrators and department heads have full access.
 *      - RH department employees have full access to all positions.
 *      - Regular employees can only read positions.
 *
 * <p>
 * Supported actions (via POST 'action' parameter):
 * - create : Create a new position
 * - update : Update an existing position
 * - delete : Delete a position
 *
 * <p>
 * Positions are stored in session and applied to GET requests.
 */
@WebServlet("/positions")
public class PositionServlet extends HttpServlet {

    private PositionDAO positionDAO;
    private UserDAO userDAO;

    /**
     * Initializes the servlet by creating DAO instances.
     *
     * @throws ServletException if DAO initialization fails
     */
    @Override
    public void init() throws ServletException {
        try {
            positionDAO = new PositionDAO();
            userDAO = new UserDAO();
            System.out.println("[INFO] PositionServlet initialisé");
        } catch (Exception e) {
            throw new ServletException("Impossible d'initialiser les DAO : " + e.getMessage());
        }
    }

    /**
     * Cleans up resources when the servlet is destroyed.
     */
    @Override
    public void destroy() {
        System.out.println("PositionServlet détruit");
        super.destroy();
    }

    /**
     * Handles GET requests.
     * Retrieves positions according to the user's role,
     * then forwards to positions.jsp.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs during redirect or forward
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");

        // Security check: redirect if no user in session
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Generate CSRF token if not present
        String csrfToken = (String) session.getAttribute("csrfToken");
        if (csrfToken == null) {
            csrfToken = new BigInteger(130, new SecureRandom()).toString(32);
            session.setAttribute("csrfToken", csrfToken);
        }

        // Load positions based on user role
        List<Position> positions = loadPositionsForUser(user);

        request.setAttribute("positions", positions);
        request.setAttribute("csrfToken", csrfToken);

        request.getRequestDispatcher("positions.jsp").forward(request, response);
    }

    /**
     * Handles POST requests.
     * Dispatches actions: create, update, delete.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if an error occurs during processing or forwarding
     * @throws IOException      if an I/O error occurs during redirect or forward
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = safeTrim(request.getParameter("action"));
        String sessionToken = (String) session.getAttribute("csrfToken");
        String requestToken = request.getParameter("csrfToken");

        // CSRF Protection
        if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {
            System.out.println("[SECURITY][Servlet] CSRF token invalide pour l'utilisateur : " + user.getFullName());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalide");
            return;
        }

        if (action == null) {
            request.setAttribute("error", "Action manquante.");
            System.out.println("[ERROR][Servlet] Action manquante dans la requête.");
            doGet(request, response);
            return;
        }

        // Mapping string action → enum Action
        Action requiredAction = mapActionString(action);

        // Special case: RH department employees have full position access
        boolean isRH = user.getRole() == Role.EMPLOYE && user.getDepartment() != null && "RH".equalsIgnoreCase(user.getDepartment().getCode());
        if (!isRH && requiredAction != null && !RolePermissions.hasPermission(user.getRole(), requiredAction)) {
            request.setAttribute("error", "Vous n'avez pas la permission pour cette action.");
            System.out.println("[SECURITY][Servlet] Permission refusée pour " + user.getFullName() + " sur l'action " + action);
            doGet(request, response);
            return;
        }

        try {
            switch (action) {
                case "create" -> createPosition(request, response);
                case "update" -> updatePosition(request, response);
                case "delete" -> deletePosition(request, response);
                default -> {
                    request.setAttribute("error", "Action invalide: " + action);
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
     * Creates a new position based on request parameters.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if an error occurs during processing or forwarding
     * @throws IOException      if an I/O error occurs during redirect or forward
     */
    private void createPosition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = safeTrim(request.getParameter("name"));
        String description = safeTrim(request.getParameter("description"));

        if (name == null || name.isEmpty() || name.length() > 100) {
            request.setAttribute("error", "Nom du poste invalide (doit être entre 1 et 100 caractères)");
            System.out.println("[ERROR][Servlet] Nom du poste invalide lors de la création");
            doGet(request, response);
            return;
        }

        // Check if position with same name already exists
        if (positionDAO.findByName(name).isPresent()) {
            request.setAttribute("error", "Un poste avec ce nom existe déjà");
            System.out.println("[ERROR][Servlet] Poste déjà existant: " + name);
            doGet(request, response);
            return;
        }

        Position position = new Position(name, description);
        Position saved = positionDAO.save(position);

        if (saved != null) {
            System.out.println("[SUCCESS][Servlet] Poste créé: " + name);
            response.sendRedirect("positions");
        } else {
            request.setAttribute("error", "Erreur lors de la création du poste.");
            System.out.println("[ERROR][Servlet] Échec de la création du poste: " + name);
            doGet(request, response);
        }
    }

    /**
     * Updates an existing position based on request parameters.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if an error occurs during processing or forwarding
     * @throws IOException      if an I/O error occurs during redirect or forward
     */
    private void updatePosition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = safeTrim(request.getParameter("name"));
        String description = safeTrim(request.getParameter("description"));

        Position position = positionDAO.findById(id).orElse(null);
        if (position == null) {
            request.setAttribute("error", "Poste introuvable, ID=" + id);
            System.out.println("[ERROR][Servlet] Impossible de mettre à jour poste, introuvable ID=" + id);
            doGet(request, response);
            return;
        }

        if (name == null || name.isEmpty() || name.length() > 100) {
            request.setAttribute("error", "Nom du poste invalide (doit être entre 1 et 100 caractères)");
            System.out.println("[ERROR][Servlet] Nom du poste invalide lors de la mise à jour");
            doGet(request, response);
            return;
        }

        // Check if another position with same name already exists
        Position existingPosition = positionDAO.findByName(name).orElse(null);
        if (existingPosition != null && !existingPosition.getId().equals(id)) {
            request.setAttribute("error", "Un autre poste avec ce nom existe déjà");
            System.out.println("[ERROR][Servlet] Nom de poste déjà utilisé: " + name);
            doGet(request, response);
            return;
        }

        // Check if there are any changes
        boolean unchanged = position.getName().equals(name) &&
                ((position.getDescription() == null && description == null) ||
                        (position.getDescription() != null && position.getDescription().equals(description)));

        if (unchanged) {
            System.out.println("[INFO][Servlet] Aucun changement détecté pour le poste ID=" + id);
            doGet(request, response);
            return;
        }

        position.setName(name);
        position.setDescription(description);

        Position updated = positionDAO.update(position);
        if (updated != null) {
            System.out.println("[SUCCESS][Servlet] Poste mis à jour, ID=" + id);
            response.sendRedirect("positions");
        } else {
            request.setAttribute("error", "Erreur lors de la mise à jour du poste.");
            System.out.println("[ERROR][Servlet] Échec mise à jour poste, ID=" + id);
            doGet(request, response);
        }
    }

    /**
     * Deletes a position based on the provided ID.
     * Checks if there are users assigned to the position before deletion.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException if an I/O error occurs during redirect or forward
     */
    private void deletePosition(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        int id = Integer.parseInt(request.getParameter("id"));

        // Check if there are users assigned to this position
        long userCount = positionDAO.countUsersByPosition(id);
        if (userCount > 0) {
            request.setAttribute("error", "Impossible de supprimer le poste : " + userCount + " utilisateur(s) y sont assigné(s)");
            System.out.println("[ERROR][Servlet] Tentative de suppression d'un poste avec utilisateurs, ID=" + id);
            doGet(request, response);
            return;
        }

        boolean success = positionDAO.deleteById(id);
        if (success) {
            System.out.println("[SUCCESS][Servlet] Poste supprimé, ID=" + id);
        } else {
            System.out.println("[ERROR][Servlet] Échec suppression poste, ID=" + id);
        }
        response.sendRedirect("positions");
    }

    /**
     * Loads positions for a given user, applying role-based access control.
     *
     * @param user Current user
     * @return List of Position objects accessible by the user
     */
    private List<Position> loadPositionsForUser(User user) {
        // Access denied if no READ_POSITION permission
        if (!RolePermissions.hasPermission(user.getRole(), Action.READ_POSITION)) {
            return Collections.emptyList();
        }

        // Special handling for employees in RH department
        boolean isRHDepartment = user.getDepartment() != null &&
                "RH".equalsIgnoreCase(user.getDepartment().getCode());

        switch (user.getRole()) {
            case ADMINISTRATEUR, CHEF_DEPARTEMENT -> {
                // Complete access
                return positionDAO.findAll();
            }
            case EMPLOYE, CHEF_PROJET -> {
                // Complete access for RH department employees
                if (isRHDepartment) {
                    return positionDAO.findAll();
                }
                // Regular employees can only read positions
                return positionDAO.findAll();
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Maps string action parameter to Action enum.
     *
     * @param action String action
     * @return Corresponding Action enum or null if invalid
     */
    private Action mapActionString(String action) {
        return switch (action) {
            case "create" -> Action.CREATE_POSITION;
            case "update" -> Action.UPDATE_POSITION;
            case "delete" -> Action.DELETE_POSITION;
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