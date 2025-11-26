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
import java.math.BigDecimal;
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
        System.out.println("[INFO][SERVLET] UserServlet initialisé");
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

        if (!RolePermissions.canAccessUserList(currentUser)) {
            session.setAttribute("errorMessage", "Accès refusé : Vous n'avez pas les permissions d'accéder à cette page.");
            System.out.println("[SECURITY][SERVLET] Accès refusé pour l'employé : " + currentUser.getFullName());
            session.setAttribute("errorType", "danger");
            response.sendRedirect("dashboard.jsp");
            return;
        }
        System.out.println("Action demandée : " + action);
        if (action == null) action = "list";
        switch (action) {
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

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        List<User> users = userDAO.search(deptId, posId, null, role, grade, searchText);
        // remove admin and rh on search if current user is not allowed to view all users
        if (!RolePermissions.canViewAllUsers(currentUser)) {
            users.removeIf(u -> u.getRole() == Role.ADMINISTRATEUR || RolePermissions.isRH(u));
        }
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

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            if (userDAO.deleteById(id)) {
                System.out.println("Utilisateur supprimé : ID=" + id);
            } else {
                System.err.println("Échec suppression ID=" + id);
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
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        List<User> users;
        if(RolePermissions.canViewAllUsers(currentUser)) { users = userDAO.findAllWithFetch();}
        else { users = userDAO.findAllExcludingAdminAndRH();}
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

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            if (!RolePermissions.canDeleteUser(currentUser)) {
                request.setAttribute("error", "Vous n'avez pas la permission de supprimer cet employé.");
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
        String contractTypeStr = request.getParameter("typeContrat");
        String salaryStr = request.getParameter("salaire");

        System.out.println("Données reçues : id=" + idStr + ", nom=" + lastName + ", prénom=" + firstName + ", email=" + email + ", role=" + roleStr + ", department=" + deptStr + ", position=" + posStr + ", grade=" + gradeStr + ", typeContrat=" + contractTypeStr + ", salaire=" + salaryStr);
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

        User targetUser = u; // u is the user being created/edited
        boolean canUpdatePrivate = RolePermissions.canUpdatePrivateInfo(currentUser, targetUser);
        boolean canUpdatePublic = RolePermissions.canUpdatePublicInfo(currentUser, targetUser);
        boolean canUpdateSalary = RolePermissions.canUpdateSalary(currentUser, targetUser);
        if(canUpdatePrivate) {
            u.setLastName(lastName);
            u.setFirstName(firstName);
            u.setEmail(email);
            u.setPhone(phone);
            u.setAddress(address);
            u.setContractType(ContractType.PERMANENT_FULL_TIME);

            Role requestedRole;
            try {
                requestedRole = Role.valueOf(roleStr);
                String roleError = RolePermissions.validateRoleAssignment(
                        currentUser, targetUser, requestedRole, deptStr, userDAO, departmentDAO
                );
                if (roleError != null) {
                    request.setAttribute("error", roleError);
                    listUsers(request, response);
                    return;
                }
                u.setRole(requestedRole);
            } catch (Exception e) {
                u.setRole(Role.EMPLOYE);
            }

            if (deptStr != null && !deptStr.isBlank()) {
                try {
                    Integer deptId = Integer.parseInt(deptStr);
                    String deptError = RolePermissions.validateDepartmentAssignment(currentUser, deptId, departmentDAO);
                    if (deptError != null) {
                        request.setAttribute("error", deptError);
                        listUsers(request, response);
                        return;
                    }
                    departmentDAO.findById(deptId).ifPresent(u::setDepartment);
                }catch (Exception ignored) {}
            }
            if (contractTypeStr != null && !contractTypeStr.trim().isEmpty()) {
                try {
                    u.setContractType(ContractType.valueOf(contractTypeStr));
                } catch (IllegalArgumentException e) {
                    u.setContractType(ContractType.PERMANENT_FULL_TIME); // valeur par défaut
                }
            }
            else {
                u.setContractType(ContractType.PERMANENT_FULL_TIME);
            }
        }

        if(canUpdatePublic) {
            if (gradeStr == null || gradeStr.trim().isEmpty()) {
                u.setGrade(null);
            } else {
                try {
                    u.setGrade(Grade.valueOf(gradeStr.trim()));
                } catch (IllegalArgumentException e) {
                    System.out.println("Grade invalide : '" + gradeStr + "'");
                    u.setGrade(null);
                }
            }

            try {
                if (posStr != null && !posStr.isBlank()) {
                    Integer posId = Integer.parseInt(posStr);
                    positionDAO.findById(posId).ifPresent(u::setPosition);
                }
            } catch (Exception ignored) {
            }
        }
        if (canUpdateSalary) {
            try {
                if (salaryStr != null && !salaryStr.isBlank()) {
                    BigDecimal salary = new BigDecimal(salaryStr);
                    // Vérifie que le salaire est entre 0 et 1 000 000 000 inclus
                    if (salary.compareTo(BigDecimal.ZERO) < 0 || salary.compareTo(new BigDecimal("1000000000")) > 0) {
                        throw new IllegalArgumentException("Salaire hors limites");
                    }
                    u.setBaseSalary(salary);
                }
            } catch (Exception ignored) {
                // Si erreur, le salaire reste à zéro (valeur par défaut du constructeur)
            }
        }
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
            if (!canUpdatePrivate && !canUpdatePublic) {
                request.setAttribute("error", "Vous n'avez pas la permission de modifier cet employé.");
                System.out.println("[SECURITY][SERVLET] Permission refusée UPDATE_USER pour " + currentUser.getFullName());
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
            if (!RolePermissions.canCreateUser(currentUser) && !RolePermissions.hasPermission(currentUser.getRole(), Action.CREATE_USER)) {
                request.setAttribute("error", "Vous n'avez pas la permission de créer un employé.");
                System.out.println("[SECURITY][SERVLET] Permission refusée CREATE_USER pour " + currentUser.getFullName());
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