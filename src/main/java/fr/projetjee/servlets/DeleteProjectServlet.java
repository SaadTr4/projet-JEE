package fr.projetjee.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import fr.projetjee.models.Project;

@WebServlet("/DeleteProjectServlet")
public class DeleteProjectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        List<Project> projects = ListProjectsServlet.getProjects();

        projects.removeIf(p -> p.getId() == id);
        response.sendRedirect("projects");
    }
}
