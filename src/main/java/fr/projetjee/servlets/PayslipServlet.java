package fr.projetjee.servlets;

import fr.projetjee.dao.PayslipDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.model.Payslip;
import fr.projetjee.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet("/payslips")
public class PayslipServlet extends HttpServlet {

    private PayslipDAO payslipDAO;
    private UserDAO userDAO;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();

        String userFilter = (String) session.getAttribute("filter_user");
        Integer yearFilter = (Integer) session.getAttribute("filter_year");
        Integer monthFilter = (Integer) session.getAttribute("filter_month");

        List<Payslip> payslips;

        if ((userFilter != null && !userFilter.isEmpty()) ||
                (yearFilter != null  || monthFilter != null )) {

            payslips = payslipDAO.findFiltered(userFilter, yearFilter, monthFilter);

        } else {
            payslips = payslipDAO.findAll();
        }

        request.setAttribute("payslips", payslips);
        request.setAttribute("employees", userDAO.findAll()); // pour datalist employés

        // Pré-remplir les filtres pour le JSP
        request.setAttribute("filter_user", userFilter != null ? userFilter : "");
        request.setAttribute("filter_year", yearFilter);
        request.setAttribute("filter_month", monthFilter);

        request.getRequestDispatcher("payslips.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action == null) {
            request.setAttribute("error", "Action manquante.");
            doGet(request, response);
            return;
        }

        try {
            switch (action) {
                case "create":
                    createPayslip(request, response);
                    break;
                case "delete":
                    deletePayslip(request, response);
                    break;
                case "update":
                    updatePayslip(request, response);
                    break;
                case "filter":
                    applyFilters(request, response);
                    break;
                case "reset":
                    resetFilters(request, response);
                    break;
                case "export":
                    exportPDF(request, response);
                    break;
                default:
                    request.setAttribute("error", "Action invalide: " + action);
                    System.out.println("[ERROR][Servlet] Action invalide reçue: " + action);
                    doGet(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur serveur: " + e.getMessage());
            doGet(request, response);
        }
    }

    private void createPayslip(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String matricule = safeTrim(request.getParameter("user"));
        Integer year = Integer.parseInt(request.getParameter("year"));
        Integer month = Integer.parseInt(request.getParameter("month"));
        BigDecimal baseSalary = new BigDecimal(request.getParameter("baseSalary"));
        BigDecimal bonuses = new BigDecimal(request.getParameter("bonuses"));
        BigDecimal deductions = new BigDecimal(request.getParameter("deductions"));

        User user = userDAO.findByMatricule(matricule).orElse(null);
        if (user == null) {
            request.setAttribute("error", "Utilisateur introuvable: " + matricule);
            System.out.println("[ERROR][Servlet] Impossible de créer fiche, utilisateur introuvable: " + matricule);
            doGet(request, response);
            return;
        }

        Payslip payslip = new Payslip(year, month, baseSalary, bonuses, deductions, user);
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

    private void updatePayslip(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));

        Payslip payslip = payslipDAO.findById(id).orElse(null);
        if (payslip == null) {
            request.setAttribute("error", "Fiche de paie introuvable, ID=" + id);
            System.out.println("[ERROR][Servlet] Impossible de mettre à jour fiche, introuvable ID=" + id);
            doGet(request, response);
            return;
        }

        BigDecimal baseSalary = new BigDecimal(request.getParameter("baseSalary"));
        BigDecimal bonuses = new BigDecimal(request.getParameter("bonuses"));
        BigDecimal deductions = new BigDecimal(request.getParameter("deductions"));

        if (!validateAndReportErrors(request, payslip, baseSalary, bonuses, deductions)) {
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

    private void deletePayslip(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        boolean success = payslipDAO.deleteById(id);
        if (success) {
            System.out.println("[SUCCESS][Servlet] Fiche de paie supprimée, ID=" + id);
        } else {
            System.out.println("[ERROR][Servlet] Échec suppression fiche de paie, ID=" + id);
        }
        response.sendRedirect("payslips");
    }
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

    private void resetFilters(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("filter_user");
        session.removeAttribute("filter_year");
        session.removeAttribute("filter_month");

        response.sendRedirect("payslips");
    }

    private void exportPDF(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Tu ajouteras le code PDF plus tard
        res.sendRedirect("payslips?export=todo");
    }
    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    private boolean validateAndReportErrors(HttpServletRequest request, Payslip payslip, BigDecimal baseSalary, BigDecimal bonuses, BigDecimal deductions) {

        int id = payslip.getId();

        // 1. Valeurs négatives
        if (baseSalary.compareTo(BigDecimal.ZERO) < 0 ||
                bonuses.compareTo(BigDecimal.ZERO) < 0 ||
                deductions.compareTo(BigDecimal.ZERO) < 0) {

            request.setAttribute("error", "Les montants ne peuvent pas être négatifs.");
            System.out.println("[ERROR][Servlet] Montants négatifs pour fiche ID=" + id);
            return false;
        }

        // 2. Déductions > revenus
        if (deductions.compareTo(baseSalary.add(bonuses)) > 0) {
            request.setAttribute("error", "Les déductions ne peuvent pas dépasser le total des revenus.");
            System.out.println("[ERROR][Servlet] Déductions > revenus pour fiche ID=" + id);
            return false;
        }

        // 3. Aucun changement
        boolean unchanged = payslip.getBaseSalary().compareTo(baseSalary) == 0 &&
                        payslip.getBonuses().compareTo(bonuses) == 0 &&
                        payslip.getDeductions().compareTo(deductions) == 0;

        if (unchanged) {
            System.out.println("[INFO][Servlet] Aucun changement détecté pour fiche ID=" + id);
            return false;
        }

        return true;
    }

    @Override
    public void destroy() {
        System.out.println("PayslipServlet détruit");
        super.destroy();
    }

}
