package fr.projetjee.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;

@WebServlet("/EditEmployeeServlet")
public class EditEmployeeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");
        String nom = request.getParameter("nom");
        String prenom = request.getParameter("prenom");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String departement = request.getParameter("departement");
        String projet = request.getParameter("projet");
        double salaire = Double.parseDouble(request.getParameter("salaire"));

        System.out.println("✏️ Modification de l’employé ID : " + id);

        // Plus tard : update en base
        response.sendRedirect("employees.jsp");
    }
}
