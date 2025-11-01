package fr.projetjee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Vérification simple
        if ("admin".equals(username) && "1234".equals(password)) {
            HttpSession session = request.getSession();

            // On enregistre sous "userEmail" car c’est ce que dashboard.jsp vérifie
            session.setAttribute("userEmail", username);

            // Redirection vers le tableau de bord
            response.sendRedirect("dashboard.jsp");
        } else {
            request.setAttribute("error", "Nom d'utilisateur ou mot de passe incorrect !");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }
}

