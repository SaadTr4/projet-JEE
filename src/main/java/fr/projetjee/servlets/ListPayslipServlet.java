package fr.projetjee.servlets;

import fr.projetjee.models.Payslip;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/payslips")
public class ListPayslipServlet extends HttpServlet {
    private static final List<Payslip> payslips = new ArrayList<>();

    static {
        payslips.add(new Payslip(1, "Jean Dupont", 3500, 200, 100));
        payslips.add(new Payslip(2, "Sophie Martin", 3000, 150, 50));
        payslips.add(new Payslip(3, "Karim Benali", 4800, 300, 0));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("payslips", payslips);
        req.getRequestDispatcher("payslips.jsp").forward(req, resp);
    }
}

