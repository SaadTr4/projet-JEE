package fr.projetjee.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import fr.projetjee.models.Project;

@WebServlet("/EditProjectServlet")
public class EditProjectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        List<Project> projects = ListProjectsServlet.getProjects();

        Project projectToEdit = null;
        for (Project p : projects) {
            if (p.getId() == id) {
                projectToEdit = p;
                break;
            }
        }

        if (projectToEdit != null) {
            request.setAttribute("project", projectToEdit);
            RequestDispatcher dispatcher = request.getRequestDispatcher("edit-project.jsp");
            dispatcher.forward(request, response);
        } else {
            response.sendRedirect("projects");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        String nom = request.getParameter("nom");
        String chefProjet = request.getParameter("chefProjet");
        String statut = request.getParameter("statut");

        List<Project> projects = ListProjectsServlet.getProjects();

        for (Project p : projects) {
            if (p.getId() == id) {
                p.setNom(nom);
                p.setChefProjet(chefProjet);
                p.setStatut(statut);
                break;
            }
        }

        response.sendRedirect("projects");
    }
}
