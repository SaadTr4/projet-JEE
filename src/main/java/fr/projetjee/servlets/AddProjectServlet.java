package fr.projetjee.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import fr.projetjee.models.Project;

@WebServlet("/AddProjectServlet")
public class AddProjectServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nom = request.getParameter("nom");
        String chefProjet = request.getParameter("chefProjet");
        String statut = request.getParameter("statut");

        List<Project> projects = ListProjectsServlet.getProjects();
        int newId = projects.size() + 1;

        projects.add(new Project(newId, nom, chefProjet, statut));
        response.sendRedirect("projects");
    }
}
