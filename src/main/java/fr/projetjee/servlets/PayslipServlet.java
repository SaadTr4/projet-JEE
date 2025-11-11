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
    public void init() {
        payslipDAO = new PayslipDAO();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String registrationNumber = request.getParameter("user");

        List<Payslip> payslips;
        if (registrationNumber != null && !registrationNumber.isEmpty()) {
            User user = userDAO.findByMatricule(registrationNumber).orElse(null);
            if (user == null) {
                request.setAttribute("error", "Utilisateur introuvable: " + registrationNumber);
                System.out.println("[ERROR][Servlet] Utilisateur introuvable pour matricule: " + registrationNumber);
                payslips = List.of();
            } else {
                payslips = payslipDAO.findByUser(user);
                System.out.println("[INFO][Servlet] " + payslips.size() + " fiche(s) trouvée(s) pour " + registrationNumber);
            }
        } else {
            payslips = payslipDAO.findAll();
            System.out.println("[INFO][Servlet] Liste complète des fiches de paie récupérée, total: " + payslips.size());
        }

        request.setAttribute("payslips", payslips);
        request.getRequestDispatcher("payslips.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            switch (action) {
                case "create":
                    createPayslip(request, response);
                    break;
                case "delete":
                    deletePayslip(request, response);
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
        String registrationNumber = request.getParameter("user");
        int year = Integer.parseInt(request.getParameter("year"));
        int month = Integer.parseInt(request.getParameter("month"));
        BigDecimal baseSalary = new BigDecimal(request.getParameter("baseSalary"));
        BigDecimal bonuses = new BigDecimal(request.getParameter("bonuses"));
        BigDecimal deductions = new BigDecimal(request.getParameter("deductions"));

        User user = userDAO.findByMatricule(registrationNumber).orElse(null);
        if (user == null) {
            request.setAttribute("error", "Utilisateur introuvable: " + registrationNumber);
            System.out.println("[ERROR][Servlet] Impossible de créer fiche, utilisateur introuvable: " + registrationNumber);
            doGet(request, response);
            return;
        }

        Payslip payslip = new Payslip(year, month, baseSalary, bonuses, deductions, user);
        Payslip saved = payslipDAO.save(payslip);
        if (saved != null) {
            System.out.println("[SUCCESS][Servlet] Fiche de paie créée pour " + registrationNumber + " mois: " + month + " année: " + year);
            response.sendRedirect("payslips?user=" + registrationNumber);
        } else {
            request.setAttribute("error", "Erreur lors de la création de la fiche de paie.");
            System.out.println("[ERROR][Servlet] Échec de la création fiche de paie pour " + registrationNumber);
            doGet(request, response);
        }
    }

    private void deletePayslip(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        //boolean success = payslipDAO.delete(id);
        /*if (success) {
            System.out.println("[SUCCESS][Servlet] Fiche de paie supprimée, ID=" + id);
        } else {
            System.out.println("[ERROR][Servlet] Échec suppression fiche de paie, ID=" + id);
        }*/
        response.sendRedirect("payslips");
    }
}
