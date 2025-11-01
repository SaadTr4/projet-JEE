package fr.projetjee.servlets;

import fr.projetjee.models.Employee;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/employees")
public class ListEmployeesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Vérifie si l'utilisateur est connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Données fictives (en attendant la BDD)
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(1, "Dupont", "Jean", "jean.dupont@entreprise.com", "Développeur", "Informatique", "Projet X", 3500));
        employees.add(new Employee(2, "Martin", "Sophie", "sophie.martin@entreprise.com", "RH", "Ressources Humaines", "-", 3000));
        employees.add(new Employee(3, "Benali", "Karim", "karim.benali@entreprise.com", "Chef Projet", "Informatique", "Projet Alpha", 4800));

        // Envoie la liste à la JSP
        request.setAttribute("employees", employees);
        RequestDispatcher dispatcher = request.getRequestDispatcher("employees.jsp");
        dispatcher.forward(request, response);
    }
}

