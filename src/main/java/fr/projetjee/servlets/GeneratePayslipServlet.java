package fr.projetjee.servlets;

import fr.projetjee.models.Payslip;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/GeneratePayslipServlet")
public class GeneratePayslipServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String employeNom = req.getParameter("employeNom");
        double salaire = Double.parseDouble(req.getParameter("salaire"));
        double prime = Double.parseDouble(req.getParameter("prime"));
        double deduction = Double.parseDouble(req.getParameter("deduction"));

        List<Payslip> payslips = (List<Payslip>) getServletContext().getAttribute("payslips");
        if (payslips == null) payslips = new java.util.ArrayList<>();

        int id = payslips.size() + 1;
        payslips.add(new Payslip(id, employeNom, salaire, prime, deduction));

        getServletContext().setAttribute("payslips", payslips);
        resp.sendRedirect("payslips");
    }
}
