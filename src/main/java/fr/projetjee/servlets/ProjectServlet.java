package fr.projetjee.servlets;

import fr.projetjee.enums.Status;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;
import fr.projetjee.model.*;
import fr.projetjee.dao.*;

@WebServlet("/projects")
public class ProjectServlet extends HttpServlet {

    private ProjectDAO projectDAO;

    @Override
    public void init() {
        projectDAO = new ProjectDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        System.out.println("➡️ doPost() appelé, action=" + action);

        try {
            switch (action) {
                case "register":
                    addProject(request, response);
                    break;

                case "update":
                    updateProject(request, response);
                    break;

                case "delete":
                    deleteProject(request, response);
                    break;

                default:
                    request.setAttribute("error", "Action invalide.");
                    doGet(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur serveur: " + e.getMessage());
            doGet(request, response);
        }
    }

    private void addProject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("nom");
        String manager = request.getParameter("chefProjet");
        String statutParam = request.getParameter("statut");

        if (name == null || name.isEmpty()) {
            request.setAttribute("error", "Le nom du projet est obligatoire.");
            doGet(request, response);
            return;
        }

        Status statut;
        try {
            statut = Status.valueOf(statutParam);
        } catch (Exception e) {
            statut = Status.IN_PROGRESS;
        }

        Project project = new Project(name, manager, statut);
        Project saved = projectDAO.save(project);
        if (saved != null) {
            response.sendRedirect("projects");
        } else {
            request.setAttribute("error", "Erreur lors de l'ajout du projet.");
            doGet(request, response);
        }
    }

    private void updateProject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String name = request.getParameter("nom");
        String manager = request.getParameter("chefProjet");
        String statutParam = request.getParameter("statut");

        if (idParam == null || idParam.isEmpty()) {
            request.setAttribute("error", "ID du projet manquant.");
            doGet(request, response);
            return;
        }

        Integer id = Integer.parseInt(idParam);
        Project project = projectDAO.findById(id).orElse(null);

        if (project == null) {
            request.setAttribute("error", "Projet introuvable.");
            doGet(request, response);
            return;
        }

        project.setName(name);
        project.setProjectManager(manager);
        try {
            project.setStatus(Status.valueOf(statutParam));
        } catch (Exception e) {
            project.setStatus(Status.IN_PROGRESS);
        }

        projectDAO.update(project);
        response.sendRedirect("projects");
    }

    private void deleteProject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            request.setAttribute("error", "ID du projet manquant.");
            doGet(request, response);
            return;
        }

        Integer id = Integer.parseInt(idParam);
        projectDAO.delete(id);
        response.sendRedirect("projects");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Project> projects = projectDAO.findAll();
        request.setAttribute("projects", projects);
        request.getRequestDispatcher("projects.jsp").forward(request, response);
    }
}
