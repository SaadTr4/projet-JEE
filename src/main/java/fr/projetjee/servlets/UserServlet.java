package fr.projetjee.servlets;

import fr.projetjee.dao.UserDAO;
import fr.projetjee.dao.DepartmentDAO;
import fr.projetjee.dao.PositionDAO;
import fr.projetjee.model.User;
import fr.projetjee.model.Department;
import fr.projetjee.model.Position;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.enums.ContractType;
import fr.projetjee.enums.Action;
import fr.projetjee.security.RolePermissions;

import fr.projetjee.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@WebServlet("/user")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;
    private DepartmentDAO departmentDAO;
    private PositionDAO positionDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
        departmentDAO = new DepartmentDAO();
        positionDAO = new PositionDAO();
        System.out.println("✔ UserServlet initialisé");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            response.sendRedirect("login");
            return;
        }

        boolean isRH = currentUser.getRole() == Role.EMPLOYE &&
                currentUser.getDepartment() != null &&
                "RH".equalsIgnoreCase(currentUser.getDepartment().getCode());

        //  BLOQUER LES EMPLOYÉS NORMAUX
        if (currentUser.getRole() == Role.EMPLOYE && !isRH) {
            System.out.println("🚫 [SECURITY] Accès refusé pour l'employé : " + currentUser.getFullName());

            //  AJOUTER UN MESSAGE D'ERREUR DANS LA SESSION
            session.setAttribute("errorMessage", "🚫 Accès refusé : Vous n'avez pas les permissions pour accéder à la liste des employés.");
            session.setAttribute("errorType", "danger");

            response.sendRedirect("dashboard.jsp");
            return;
        }

        if (!isRH && !RolePermissions.hasPermission(currentUser.getRole(), Action.READ_USER)) {
            request.setAttribute("error", "Vous n'avez pas la permission d'accéder à cette page.");
            response.sendRedirect("dashboard.jsp");
            return;
        }

        if (action == null) action = "list";

        switch (action) {
            case "edit":
                handleEdit(request, response);
                break;
            case "image":
                serveImage(request, response);
                break;
            case "search":
                handleSearch(request, response);
                break;
            default:
                listUsers(request, response);
                break;
        }
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String deptIdStr = request.getParameter("searchDepartment");
        String posIdStr = request.getParameter("searchPosition");
        String roleStr = request.getParameter("searchRole");
        String gradeStr = request.getParameter("searchGrade");
        String searchText = request.getParameter("searchText");

        Integer deptId = null;
        Integer posId = null;
        Role role = null;
        Grade grade = null;

        try {
            if (deptIdStr != null && !deptIdStr.isBlank() && !deptIdStr.equals("all")) {
                deptId = Integer.parseInt(deptIdStr);
            }
        } catch (Exception ignored) {}

        try {
            if (posIdStr != null && !posIdStr.isBlank() && !posIdStr.equals("all")) {
                posId = Integer.parseInt(posIdStr);
            }
        } catch (Exception ignored) {}

        try {
            if (roleStr != null && !roleStr.isBlank() && !roleStr.equals("all")) {
                role = Role.valueOf(roleStr);
            }
        } catch (Exception ignored) {}

        try {
            if (gradeStr != null && !gradeStr.isBlank() && !gradeStr.equals("all")) {
                grade = Grade.valueOf(gradeStr);
            }
        } catch (Exception ignored) {}

        List<User> users = userDAO.search(deptId, posId, null, role, grade, searchText);

        request.setAttribute("users", users);
        request.setAttribute("searchActive", true);
        request.setAttribute("searchCount", users.size());
        request.setAttribute("lastSearchDept", deptIdStr);
        request.setAttribute("lastSearchPos", posIdStr);
        request.setAttribute("lastSearchRole", roleStr);
        request.setAttribute("lastSearchGrade", gradeStr);
        request.setAttribute("lastSearchText", searchText);
        request.setAttribute("departments", departmentDAO.findAll());
        request.setAttribute("positions", positionDAO.findAll());

        System.out.println(" Recherche effectuée : " + users.size() + " résultat(s)");

        request.getRequestDispatcher("employees.jsp").forward(request, response);
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Optional<User> userOpt = userDAO.findById(id);

            if (userOpt.isPresent()) {
                request.setAttribute("userEdit", userOpt.get());
                request.setAttribute("editMode", true);
            } else {
                request.setAttribute("error", "Utilisateur introuvable");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "ID invalide");
        }

        listUsers(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            if (userDAO.deleteById(id)) {
                System.out.println("✅ Utilisateur supprimé : ID=" + id);
            } else {
                System.err.println("❌ Échec suppression ID=" + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("user");
    }

    private void serveImage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Optional<User> userOpt = userDAO.findById(id);

            if (userOpt.isPresent() && userOpt.get().getImage() != null) {
                byte[] image = userOpt.get().getImage();
                response.setContentType("image/jpeg");
                response.setContentLength(image.length);
                response.getOutputStream().write(image);
                response.getOutputStream().flush();
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void listUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<User> users = userDAO.findAllWithFetch();
        request.setAttribute("users", users);
        request.setAttribute("departments", departmentDAO.findAll());
        request.setAttribute("positions", positionDAO.findAll());
        request.getRequestDispatcher("employees.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            response.sendRedirect("login");
            return;
        }

        boolean isRH = currentUser.getRole() == Role.EMPLOYE &&
                currentUser.getDepartment() != null &&
                "RH".equalsIgnoreCase(currentUser.getDepartment().getCode());

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            if (!isRH && !RolePermissions.hasPermission(currentUser.getRole(), Action.DELETE_USER)) {
                request.setAttribute("error", "Vous n'avez pas la permission de supprimer un employé.");
                System.out.println("[SECURITY] Permission refusée DELETE_USER pour " + currentUser.getFullName());
                listUsers(request, response);
                return;
            }
            handleDelete(request, response);
            return;
        }

        String idStr = request.getParameter("id");
        String lastName = request.getParameter("nom");
        String firstName = request.getParameter("prenom");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String roleStr = request.getParameter("role");
        String gradeStr = request.getParameter("grade");
        String deptStr = request.getParameter("department");
        String posStr = request.getParameter("position");

        if (lastName == null || lastName.isBlank() ||
                firstName == null || firstName.isBlank() ||
                email == null || email.isBlank()) {
            request.setAttribute("error", "Les champs nom, prénom et email sont obligatoires");
            listUsers(request, response);
            return;
        }

        Optional<User> byEmail = userDAO.findByEmail(email);
        if (idStr == null || idStr.isEmpty()) {
            if (byEmail.isPresent()) {
                request.setAttribute("error", "Cet email existe déjà");
                listUsers(request, response);
                return;
            }
        } else {
            if (byEmail.isPresent() && !byEmail.get().getId().equals(Integer.parseInt(idStr))) {
                request.setAttribute("error", "Cet email appartient déjà à un autre utilisateur");
                listUsers(request, response);
                return;
            }
        }

        User u;
        if (idStr != null && !idStr.isEmpty()) {
            // ===== MODE ÉDITION =====
            int id = Integer.parseInt(idStr);
            u = userDAO.findById(id).orElse(new User());
            u.setId(id);
            //  NE PAS MODIFIER LE MOT DE PASSE lors de l'édition
        } else {
            // ===== MODE CRÉATION =====
            u = new User();
            u.setMatricule(userDAO.generateMatricule());
            //  HACHER LE MOT DE PASSE
            u.setPassword(PasswordUtil.hashPassword("motdepasse123"));
            //System.out.println(" Nouvel employé créé avec mot de passe haché");
        }

        u.setLastName(lastName);
        u.setFirstName(firstName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setAddress(address);
        u.setContractType(ContractType.PERMANENT_FULL_TIME);

        try {
            u.setRole(Role.valueOf(roleStr));
        } catch (Exception e) {
            u.setRole(Role.EMPLOYE);
        }

        try {
            if (gradeStr != null && !gradeStr.isBlank()) {
                u.setGrade(Grade.valueOf(gradeStr));
            }
        } catch (Exception ignored) {}

        try {
            if (deptStr != null && !deptStr.isBlank()) {
                Integer deptId = Integer.parseInt(deptStr);
                departmentDAO.findById(deptId).ifPresent(u::setDepartment);
            }
        } catch (Exception ignored) {}

        try {
            if (posStr != null && !posStr.isBlank()) {
                Integer posId = Integer.parseInt(posStr);
                positionDAO.findById(posId).ifPresent(u::setPosition);
            }
        } catch (Exception ignored) {}

        try {
            Part imagePart = request.getPart("image");
            if (imagePart != null && imagePart.getSize() > 0) {
                String contentType = imagePart.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    try (InputStream is = imagePart.getInputStream()) {
                        byte[] imageBytes = is.readAllBytes();
                        u.setImage(imageBytes);
                        System.out.println("✅ Image uploadée : " + imageBytes.length + " bytes");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur upload image : " + e.getMessage());
        }

        User saved;
        if (idStr != null && !idStr.isEmpty()) {
            if (!isRH && !RolePermissions.hasPermission(currentUser.getRole(), Action.UPDATE_USER)) {
                request.setAttribute("error", "Vous n'avez pas la permission de modifier un employé.");
                System.out.println("[SECURITY] Permission refusée UPDATE_USER pour " + currentUser.getFullName());
                listUsers(request, response);
                return;
            }

            saved = userDAO.update(u);
            if (saved != null) {
                System.out.println("✅ Employé mis à jour : " + saved.getFullName());
            } else {
                request.setAttribute("error", "Erreur lors de la mise à jour");
                listUsers(request, response);
                return;
            }
        } else {
            if (!isRH && !RolePermissions.hasPermission(currentUser.getRole(), Action.CREATE_USER)) {
                request.setAttribute("error", "Vous n'avez pas la permission de créer un employé.");
                System.out.println("[SECURITY] Permission refusée CREATE_USER pour " + currentUser.getFullName());
                listUsers(request, response);
                return;
            }

            saved = userDAO.save(u);
            if (saved != null) {
                System.out.println("✅ Nouvel employé créé : " + saved.getFullName() + " (" + saved.getMatricule() + ")");
            } else {
                request.setAttribute("error", "Erreur lors de la création");
                listUsers(request, response);
                return;
            }
        }

        response.sendRedirect("user");
    }
}