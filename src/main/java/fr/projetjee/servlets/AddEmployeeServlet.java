package fr.projetjee.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import fr.projetjee.models.Employee;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/AddEmployeeServlet")
public class AddEmployeeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nom = request.getParameter("nom");
        String prenom = request.getParameter("prenom");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String departement = request.getParameter("departement");
        String projet = request.getParameter("projet");
        double salaire = Double.parseDouble(request.getParameter("salaire"));

        // Simulation temporaire (on fera la BDD plus tard)
        System.out.println("✅ Nouvel employé ajouté : " + nom + " " + prenom);

        // Redirection vers la page employees.jsp
        response.sendRedirect("employees.jsp");
    }
}
