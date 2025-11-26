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


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Servlet user avec support complet upload image + modal edit/add + RECHERCHE MULTICRITÈRE
 */
@WebServlet("/user")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB max
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
        System.out.println("Appel de doGet, action=" + action);

        if (action == null) action = "list";

        System.out.println("Action demandée : " + action);

        switch (action) {
            case "edit":
                handleEdit(request, response);
                break;

            case "delete":
                handleDelete(request, response);
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

    /**
     * Gère la recherche multicritère
     */
    private void handleSearch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupérer les paramètres de recherche
        String deptIdStr = request.getParameter("searchDepartment");
        String posIdStr = request.getParameter("searchPosition");
        String roleStr = request.getParameter("searchRole");
        String gradeStr = request.getParameter("searchGrade");
        String searchText = request.getParameter("searchText");

        // Conversion des paramètres
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

        // Effectuer la recherche
        List<User> users = userDAO.search(deptId, posId, null, role, grade, searchText);

        request.setAttribute("users", users);
        request.setAttribute("searchActive", true);
        request.setAttribute("searchCount", users.size());

        // Conserver les valeurs de recherche pour les afficher dans le formulaire
        request.setAttribute("lastSearchDept", deptIdStr);
        request.setAttribute("lastSearchPos", posIdStr);
        request.setAttribute("lastSearchRole", roleStr);
        request.setAttribute("lastSearchGrade", gradeStr);
        request.setAttribute("lastSearchText", searchText);

        // Charger les listes pour les selects
        request.setAttribute("departments", departmentDAO.findAll());
        request.setAttribute("positions", positionDAO.findAll());

        System.out.println(" Recherche effectuée : " + users.size() + " résultat(s)");

        request.getRequestDispatcher("employees.jsp").forward(request, response);
    }

    /**
     * Gère l'affichage du formulaire d'édition
     */
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

    /**
     * Suppression d'un utilisateur
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            if (userDAO.deleteById(id)) {
                System.out.println(" Utilisateur supprimé : ID=" + id);
            } else {
                System.err.println(" Échec suppression ID=" + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("user");
    }

    /**
     * Servir l'image d'un utilisateur
     */
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

    /**
     * Affiche la liste des utilisateurs
     */
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

        System.out.println("Appel de doPost");

        // Récupération des paramètres
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

        // Validation basique
        if (lastName == null || lastName.isBlank() ||
                firstName == null || firstName.isBlank() ||
                email == null || email.isBlank()) {
            request.setAttribute("error", "Les champs nom, prénom et email sont obligatoires");
            listUsers(request, response);
            return;
        }

        // Vérification email unique
        Optional<User> byEmail = userDAO.findByEmail(email);
        if (idStr == null || idStr.isEmpty()) {
            // Création : email doit être unique
            if (byEmail.isPresent()) {
                request.setAttribute("error", "Cet email existe déjà");
                listUsers(request, response);
                return;
            }
        } else {
            // Mise à jour : vérifier que l'email n'appartient pas à un autre user
            if (byEmail.isPresent() && !byEmail.get().getId().equals(Integer.parseInt(idStr))) {
                request.setAttribute("error", "Cet email appartient déjà à un autre utilisateur");
                listUsers(request, response);
                return;
            }
        }

        // Construction de l'objet User
        User u;
        if (idStr != null && !idStr.isEmpty()) {
            // Mode édition : récupérer l'utilisateur existant
            int id = Integer.parseInt(idStr);
            u = userDAO.findById(id).orElse(new User());
            u.setId(id);
        } else {
            // Mode création
            u = new User();
            // Génération automatique du matricule
            u.setMatricule(userDAO.generateMatricule());
        }

        // Remplir les champs
        u.setLastName(lastName);
        u.setFirstName(firstName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setAddress(address);
        u.setContractType(ContractType.PERMANENT_FULL_TIME); // Par défaut

        // Role & Grade (avec gestion d'erreur)
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

        // Relations Department / Position
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

        // Gestion de l'image (optionnelle)
        try {
            Part imagePart = request.getPart("image");
            if (imagePart != null && imagePart.getSize() > 0) {
                // Vérifier le type MIME
                String contentType = imagePart.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    try (InputStream is = imagePart.getInputStream()) {
                        byte[] imageBytes = is.readAllBytes();
                        u.setImage(imageBytes);
                        System.out.println("✅ Image uploadée : " + imageBytes.length + " bytes");
                    }
                } else {
                    System.out.println("⚠️ Fichier non-image ignoré");
                }
            }
        } catch (Exception e) {
            System.err.println(" Erreur upload image : " + e.getMessage());
            e.printStackTrace();
        }


        // Sauvegarde
        // Création
        User saved = userDAO.save(u);
        System.out.println("SAVED" + saved);
        if (saved != null) {
            System.out.println(" Nouvel employé créé : " + saved.getFullName() + " (" + saved.getMatricule() + ")");
        } else {
            request.setAttribute("error", "Erreur lors de la création");
            System.out.println("[ERROR][DAO] Erreur ");
            listUsers(request, response);
            return;
        }

        response.sendRedirect("user");
    }
}