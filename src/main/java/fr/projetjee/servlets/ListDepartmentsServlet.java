package fr.projetjee.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.*;
import fr.projetjee.models.Department;

@WebServlet("/departments")
public class ListDepartmentsServlet extends HttpServlet {
    private static final List<Department> departments = new ArrayList<>();

    static {
        departments.add(new Department(1, "Ressources Humaines", "Sophie Martin", 6));
        departments.add(new Department(2, "Informatique", "Karim Benali", 12));
        departments.add(new Department(3, "Marketing", "Jean Dupont", 8));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("departments", departments);
        RequestDispatcher dispatcher = request.getRequestDispatcher("departments.jsp");
        dispatcher.forward(request, response);
    }

    public static List<Department> getDepartments() {
        return departments;
    }
}
