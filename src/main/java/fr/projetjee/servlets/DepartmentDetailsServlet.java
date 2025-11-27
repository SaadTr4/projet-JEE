package fr.projetjee.servlets;

import fr.projetjee.dao.DepartmentDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.model.Department;
import fr.projetjee.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@WebServlet("/departmentdetails")
public class DepartmentDetailsServlet extends HttpServlet {

    private DepartmentDAO departmentDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        departmentDAO = new DepartmentDAO();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect("departments");
            return;
        }

        try {
            Integer departmentId = Integer.parseInt(idParam);
            Optional<Department> departmentOpt = departmentDAO.findById(departmentId);

            if (departmentOpt.isPresent()) {
                Department department = departmentOpt.get();

                // Récupérer les employés du département
                List<User> departmentEmployees = departmentDAO.findUsersByDepartment(departmentId);

                // Récupérer le chef de département
                User departmentHead = findDepartmentHead(departmentEmployees);

                // Générer token CSRF
                String csrfToken = new BigInteger(130, new SecureRandom()).toString(32);
                session.setAttribute("csrfToken", csrfToken);

                request.setAttribute("department", department);
                request.setAttribute("departmentEmployees", departmentEmployees);
                request.setAttribute("departmentHead", departmentHead);
                request.setAttribute("allEmployees", userDAO.findAll());
                request.setAttribute("csrfToken", csrfToken);

                request.getRequestDispatcher("departementdetails.jsp").forward(request, response);
            } else {
                response.sendRedirect("departments?error=Département non trouvé");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("departments?error=ID de département invalide");
        }
    }

    private User findDepartmentHead(List<User> departmentEmployees) {
        return departmentEmployees.stream()
                .filter(user -> user.getRole() != null &&
                        user.getRole().name().equals("CHEF_DEPARTEMENT"))
                .findFirst()
                .orElse(null);
    }
}