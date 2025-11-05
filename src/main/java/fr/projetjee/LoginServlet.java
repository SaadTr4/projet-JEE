package fr.projetjee;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupération des valeurs du formulaire
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Vérification simple (provisoire)
        if ("admin".equals(username) && "1234".equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("username", username); // ⚠️ même nom utilisé dans dashboard.jsp
            response.sendRedirect("dashboard.jsp");
        } else {
            // Message d’erreur et retour à la page de connexion
            request.setAttribute("error", "Email ou mot de passe incorrect");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }
}

