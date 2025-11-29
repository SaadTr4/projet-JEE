package fr.projetjee.servlets;

import fr.projetjee.dao.ProjectDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.model.Project;
import fr.projetjee.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private ProjectDAO projectDAO = new ProjectDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String loginInput = safeTrim(request.getParameter("login"));
        String password = request.getParameter("password");

        if (loginInput == null || password == null || loginInput.isEmpty() || password.isEmpty()) {
            request.setAttribute("error", "Email et mot de passe requis");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }

        Optional<User> userOpt = userDAO.findByEmailOrMatricule(loginInput);

        if (userOpt.isEmpty()) {
            request.setAttribute("error", "Email/matricule ou mot de passe invalide");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }

        User user = userOpt.get();

        // Vérification du mot de passe hashé
        if (!BCrypt.checkpw(password, user.getPassword())) {
            request.setAttribute("error", "Email ou mot de passe invalide");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }

        // Authentification réussie → stocker l'utilisateur en session
        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user);


        // Redirection vers la page d'accueil
        response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
    }
    private String safeTrim(String str) {
        return (str != null) ? str.trim() : null;
    }
}
