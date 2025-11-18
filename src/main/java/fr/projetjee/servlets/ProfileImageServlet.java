package fr.projetjee.servlets;

import fr.projetjee.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/profile-image")
public class ProfileImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) return;

        User user = (User) session.getAttribute("currentUser");
        if (user == null || user.getImage() == null) return;

        byte[] imageBytes = user.getImage();

        resp.setContentType("image/jpeg");
        resp.setContentLength(imageBytes.length);

        OutputStream os = resp.getOutputStream();
        os.write(imageBytes);
        os.flush();
    }
}
