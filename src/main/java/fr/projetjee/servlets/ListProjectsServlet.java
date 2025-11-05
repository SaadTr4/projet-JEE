package fr.projetjee.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.*;
import fr.projetjee.models.Project;

@WebServlet("/projects")
public class ListProjectsServlet extends HttpServlet {
    private static final List<Project> projects = new ArrayList<>();

    static {
        projects.add(new Project(1, "Projet Alpha", "Karim Benali", "En cours"));
        projects.add(new Project(2, "Projet Beta", "Sophie Martin", "Terminé"));
        projects.add(new Project(3, "Projet Gamma", "Jean Dupont", "Annulé"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("projects", projects);
        RequestDispatcher dispatcher = request.getRequestDispatcher("projects.jsp");
        dispatcher.forward(request, response);
    }

    public static List<Project> getProjects() {
        return projects;
    }
}

