package fr.projetjee.servlets;

import fr.projetjee.dao.PayslipDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.Action;
import fr.projetjee.enums.Role;
import fr.projetjee.model.Payslip;
import fr.projetjee.model.User;
import fr.projetjee.security.RolePermissions;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;


/**
 * PayslipServlet handles CRUD operations, filtering, and exporting of payslips.
 *
 * <p>
 * Security:
 * - CSRF protection: each POST request must include a valid CSRF token generated in the session during GET requests.
 * - Role-based access control:
 *      - Administrators and department heads have full access.
 *      - RH department employees have full access to all payslips.
 *      - Regular employees can only see their own payslips unless they are in RH.
 *
 * <p>
 * Supported actions (via POST 'action' parameter):
 * - create : Create a new payslip
 * - update : Update an existing payslip
 * - delete : Delete a payslip
 * - filter : Apply filters to payslip list
 * - reset  : Reset filters
 * - export : Export payslips to PDF (functionality to implement)
 *
 * <p>
 * Filters (user, year, month) are stored in session and applied to GET requests.
 */
@WebServlet("/payslips")
public class PayslipServlet extends HttpServlet {

    private PayslipDAO payslipDAO;
    private UserDAO userDAO;

    /**
     * Initializes the servlet by creating DAO instances.
     *
     * @throws ServletException if DAO initialization fails
     */
    @Override
    public void init() throws ServletException {
        try {
            payslipDAO = new PayslipDAO();
            userDAO = new UserDAO();
            System.out.println("[INFO] PayslipServlet initialisé");
        } catch (Exception e) {
            throw new ServletException("Impossible d'initialiser les DAO : " + e.getMessage());
        }
    }

    /**
     * Cleans up resources when the servlet is destroyed.
     */
    @Override
    public void destroy() {
        System.out.println("PayslipServlet détruit");
        super.destroy();
    }

    /**
     * Handles GET requests.
     * Retrieves payslips according to the user's role and applied filters,
     * then forwards to payslips.jsp.
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
            csrfToken = new BigInteger(130, new SecureRandom()).toString(32); // Generate a secure and random CSRF token
            session.setAttribute("csrfToken", csrfToken);
        }

        // Retrieve filters from session
        String userFilter = (String) session.getAttribute("filter_user");
        Integer yearFilter = (Integer) session.getAttribute("filter_year");
        Integer monthFilter = (Integer) session.getAttribute("filter_month");

        // Load payslips based on user role and filters
        List<Payslip> payslips = loadPayslipsForUser(user, userFilter, yearFilter, monthFilter);

        request.setAttribute("payslips", payslips);
        request.setAttribute("employees", userDAO.findAll()); // For datalist employees in JSP
        request.setAttribute("csrfToken", csrfToken);

        // Set filter attributes for JSP
        request.setAttribute("filter_user", userFilter != null ? userFilter : "");
        request.setAttribute("filter_year", yearFilter);
        request.setAttribute("filter_month", monthFilter);

        request.getRequestDispatcher("payslips.jsp").forward(request, response);
    }

    /**
     * Handles POST requests.
     * Dispatches actions: create, update, delete, filter, reset, export.
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

        // CSRF Protection , verify that the CSRF token in the request matches the one in the session
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
                case "create" -> createPayslip(request, response);
                case "delete" -> deletePayslip(request, response);
                case "update" -> updatePayslip(request, response);
                case "filter" -> applyFilters(request, response);
                case "reset" -> resetFilters(request, response);
                case "export" -> exportPDF(request, response);
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
     * Creates a new payslip based on request parameters.
     * Validates user existence before creation.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if an error occurs during processing or forwarding
     * @throws IOException      if an I/O error occurs during redirect or forward
     */
    private void createPayslip(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String matricule = safeTrim(request.getParameter("user"));
        Integer year = Integer.parseInt(request.getParameter("year"));
        Integer month = Integer.parseInt(request.getParameter("month"));

        BigDecimal baseSalary = parseOrZero(request.getParameter("baseSalary"));
        BigDecimal bonuses = parseOrZero(request.getParameter("bonuses"));
        BigDecimal deductions = parseOrZero(request.getParameter("deductions"));


        User user = userDAO.findByMatricule(matricule).orElse(null);
        if (user == null) {
            request.setAttribute("error", "Utilisateur introuvable: " + matricule);
            System.out.println("[ERROR][Servlet] Impossible de créer fiche, utilisateur introuvable: " + matricule);
            doGet(request, response);
            return;
        }

        // Check for existing payslip for the same month and year
        if (payslipDAO.existsPayslipForUserAndMonth(user, year, month)) {
            System.out.println("[ERROR][Servlet] Fiche de paie déjà existante pour " + matricule + " mois: " + month + " année: " + year);
            request.setAttribute("error", "Une fiche de paie existe déjà pour ce mois et cette année pour cet employé.");
            doGet(request, response);
            return;
        }

        Payslip payslip = new Payslip(year, month, baseSalary, bonuses, deductions, user);
        if (!validateAndReportErrors(request, payslip, baseSalary, bonuses, deductions, false)) {
            doGet(request, response);
            return;
        }
        Payslip saved = payslipDAO.save(payslip);
        if (saved != null) {
            System.out.println("[SUCCESS][Servlet] Fiche de paie créée pour " + matricule + " mois: " + month + " année: " + year);
            response.sendRedirect("payslips?user=" + matricule);
        } else {
            request.setAttribute("error", "Erreur lors de la création de la fiche de paie.");
            System.out.println("[ERROR][Servlet] Échec de la création fiche de paie pour " + matricule);
            doGet(request, response);
        }
    }

    /**
     * Updates an existing payslip based on request parameters.
     * Validates existence and correctness of data before updating.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException if an error occurs during processing or forwarding
     * @throws IOException      if an I/O error occurs during redirect or forward
     */
    private void updatePayslip(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));

        Payslip payslip = payslipDAO.findById(id).orElse(null);
        if (payslip == null) {
            request.setAttribute("error", "Fiche de paie introuvable, ID=" + id);
            System.out.println("[ERROR][Servlet] Impossible de mettre à jour fiche, introuvable ID=" + id);
            doGet(request, response);
            return;
        }

        BigDecimal baseSalary = parseOrZero(request.getParameter("baseSalary"));
        BigDecimal bonuses = parseOrZero(request.getParameter("bonuses"));
        BigDecimal deductions = parseOrZero(request.getParameter("deductions"));

        if (!validateAndReportErrors(request, payslip, baseSalary, bonuses, deductions, true)) {
            doGet(request, response);
            return;
        }
        payslip.setBaseSalary(baseSalary);
        payslip.setBonuses(bonuses);
        payslip.setDeductions(deductions);
        payslip.calculateNetPay();

        Payslip updated = payslipDAO.update(payslip);
        if (updated != null) {
            System.out.println("[SUCCESS][Servlet] Fiche de paie mise à jour, ID=" + id);
            response.sendRedirect("payslips");
        } else {
            request.setAttribute("error", "Erreur lors de la mise à jour de la fiche de paie.");
            System.out.println("[ERROR][Servlet] Échec mise à jour fiche de paie, ID=" + id);
            doGet(request, response);
        }
    }

    /**
     * Deletes a payslip based on the provided ID.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException      if an I/O error occurs during redirect or forward
     */
    private void deletePayslip(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        boolean success = payslipDAO.deleteById(id);
        if (success) {
            System.out.println("[SUCCESS][Servlet] Fiche de paie supprimée, ID=" + id);
        } else {
            System.out.println("[ERROR][Servlet] Échec suppression fiche de paie, ID=" + id);
        }
        response.sendRedirect("payslips");
    }

    /**
     * Applies filters from request parameters and stores them in the session.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException if an I/O error occurs during redirect
     */
    private void applyFilters(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = safeTrim(request.getParameter("user"));
        String yearStr = safeTrim(request.getParameter("year"));
        String monthStr = safeTrim(request.getParameter("month"));

        HttpSession session = request.getSession();

        session.setAttribute("filter_user", user);
        Integer yearFilter = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : null;
        session.setAttribute("filter_year", yearFilter);

        Integer monthFilter = (monthStr != null && !monthStr.isEmpty()) ? Integer.parseInt(monthStr) : null;
        session.setAttribute("filter_month", monthFilter);

        response.sendRedirect("payslips");
    }

    /**
     * Resets all filters by removing them from the session.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException if an I/O error occurs during redirect
     */
    private void resetFilters(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("filter_user");
        session.removeAttribute("filter_year");
        session.removeAttribute("filter_month");

        response.sendRedirect("payslips");
    }

    /**
     * Loads payslips for a given user, applying role-based access control
     * and optional filters for user, year, and month.
     *
     * @param user       Current user
     * @param userFilter Filter by employee matricule
     * @param yearFilter Filter by year
     * @param monthFilter Filter by month
     * @return List of Payslip objects accessible by the user
     */
    private List<Payslip> loadPayslipsForUser(User user, String userFilter, Integer yearFilter, Integer monthFilter) {

        // Access denied if no READ_PROJECT permission
        if (!RolePermissions.hasPermission(user.getRole(), Action.READ_PAYSLIP)) {
            return Collections.emptyList();
        }

        // Special handling for employees in RH department
        boolean isRHDepartment = user.getDepartment() != null &&
                "RH".equalsIgnoreCase(user.getDepartment().getCode());


        switch (user.getRole()) {
            case ADMINISTRATEUR-> {
                // Complete access
                return getFilteredOrAllPayslips(userFilter, yearFilter, monthFilter);
            }
            case EMPLOYE, CHEF_PROJET, CHEF_DEPARTEMENT  -> {
                // Complete access for RH department employees
                if (isRHDepartment) {
                    return getFilteredOrAllPayslips(userFilter, yearFilter, monthFilter);
                }
                return payslipDAO.findByUserId(user.getId());
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Returns filtered payslips or all payslips if no filter is applied.
     *
     * @param userFilter  Filter by employee matricule
     * @param yearFilter  Filter by year
     * @param monthFilter Filter by month
     * @return List of Payslip objects
     */
    private List<Payslip> getFilteredOrAllPayslips(String userFilter, Integer yearFilter, Integer monthFilter) {
        boolean hasFilters =
                (userFilter != null && !userFilter.isEmpty()) ||
                        (yearFilter != null) ||
                        (monthFilter != null);

        return hasFilters
                ? payslipDAO.findFiltered(userFilter, yearFilter, monthFilter)
                : payslipDAO.findAll();
    }


    /** Validates payslip data and sets error messages in the request if validation fails.
     *
     * @param request    HttpServletRequest to set error messages
     * @param payslip    Payslip being validated
     * @param baseSalary New base salary
     * @param bonuses    New bonuses
     * @param deductions New deductions
     * @return true if validation passes, false otherwise
     */
    private boolean validateAndReportErrors(HttpServletRequest request, Payslip payslip, BigDecimal baseSalary, BigDecimal bonuses, BigDecimal deductions, boolean isUpdate) {

        // 1. Négative values
        if (baseSalary.compareTo(BigDecimal.ZERO) < 0 ||
                bonuses.compareTo(BigDecimal.ZERO) < 0 ||
                deductions.compareTo(BigDecimal.ZERO) < 0) {

            request.setAttribute("error", "Les montants ne peuvent pas être négatifs.");
            System.out.println("[ERROR][Servlet] Montants négatifs pour la fiche" );
            return false;
        }

        // 2.  Deductions > total income
        if (deductions.compareTo(baseSalary.add(bonuses)) > 0) {
            request.setAttribute("error", "Les déductions ne peuvent pas dépasser le total des revenus.");
            System.out.println("[ERROR][Servlet] Déductions > revenus pour la fiche");
            return false;
        }

        // 3. No changes (only for updates)
        if (isUpdate) {
                boolean unchanged = payslip.getBaseSalary().compareTo(baseSalary) == 0 &&
                            payslip.getBonuses().compareTo(bonuses) == 0 &&
                            payslip.getDeductions().compareTo(deductions) == 0;

            if (unchanged) {
                System.out.println("[INFO][Servlet] Aucun changement détecté pour la fiche ");
                return false;
            }
        }

        return true;
    }

    /**
     * Maps string action parameter to Action enum.
     *
     * @param action String action
     * @return Corresponding Action enum or null if invalid
     */
    private Action mapActionString(String action) {
        return switch (action) {
            case "register" -> Action.CREATE_PAYSLIP;
            case "update" -> Action.UPDATE_PAYSLIP;
            case "delete" -> Action.DELETE_PAYSLIP;
            case "filter", "reset" -> Action.FILTER_PAYSLIP;
            default -> null;
        };
    }

    /**
     * Exports payslips to PDF (functionality to implement).
     *
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @throws IOException if an I/O error occurs during redirect
     */
    private void exportPDF(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // TODO: Implement PDF export functionality
        res.sendRedirect("payslips?export=todo");
    }

    /**
     *  Parses a string to BigDecimal, returning zero for null/empty/invalid inputs.
     *
     * @param value Input string
     * @return Parsed BigDecimal or BigDecimal.ZERO
     */
    private BigDecimal parseOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
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
