package fr.projetjee.servlets;

// ========================================
// IMPORTS
// ========================================

import fr.projetjee.dao.DepartmentDAO;
import fr.projetjee.dao.PositionDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.dto.UserDTO;
import fr.projetjee.model.Department;
import fr.projetjee.model.Position;
import fr.projetjee.model.User;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servlet REST pour la gestion complète des employés/utilisateurs
 *
 * Projet JEE - Gestion RH, Départements, Projets et Fiches de Paie
 *
 * Fonctionnalités implémentées :
 *  Ajouter un nouvel employé
 *  Modifier les informations d'un employé
 *  Supprimer un employé
 *  Lister tous les employés
 *  Lister les employés par grade, poste
 * Rechercher un employé par nom, prénom, matricule ou département
 * Gestion des images (photo) en Base64
 * Validation complète des données
 *  Gestion des erreurs HTTP appropriées
 *  CORS activé pour intégration frontend
 *
 * Endpoints disponibles:
 * GET    /api/users                     - Liste tous les employés
 * GET    /api/users/{id}                - Récupère un employé par ID
 * GET    /api/users?matricule=X         - Recherche par matricule
 * GET    /api/users?email=X             - Recherche par email
 * GET    /api/users?name=X              - Recherche par nom/prénom
 * GET    /api/users?grade=X             - Filtre par grade (JUNIOR, SENIOR, EXPERT)
 * GET    /api/users?role=X              - Filtre par rôle
 * GET    /api/users?department=X        - Filtre par département (ID)
 * GET    /api/users?position=X          - Filtre par poste (ID)
 * POST   /api/users                     - Crée un nouvel employé
 * PUT    /api/users/{id}                - Met à jour un employé existant
 * DELETE /api/users/{id}                - Supprime un employé
 *
 * Format de réponse standardisé:
 * {
 *   "success": true/false,
 *   "message": "Message descriptif (optionnel)",
 *   "data": {...} ou [...]
 * }
 *
 */
public class UserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO userDAO;
    private DepartmentDAO departmentDAO;
    private PositionDAO positionDAO;
    private Gson gson;

    // ========================================
    // INITIALISATION
    // ========================================

    @Override
    public void init() throws ServletException {
        super.init();

        System.out.println("========================================");
        System.out.println("🚀 INITIALISATION UserServlet");
        System.out.println("========================================");

        try {
            userDAO = new UserDAO();
            System.out.println(" UserDAO initialisé");

            departmentDAO = new DepartmentDAO();
            System.out.println(" DepartmentDAO initialisé");

            positionDAO = new PositionDAO();
            System.out.println(" PositionDAO initialisé");

            // Configuration Gson : sérialiser AUSSI les champs null
            gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd")
                    .serializeNulls()  //  IMPORTANT : Inclure les champs null
                    .setPrettyPrinting()
                    .create();
            System.out.println(" Gson configuré (serializeNulls activé)");

            System.out.println(" UserServlet initialisé avec succès");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de l'initialisation du servlet:");
            e.printStackTrace();
            throw new ServletException("Erreur d'initialisation UserServlet", e);
        }
    }

    // ========================================
    // MÉTHODE GET - RÉCUPÉRATION DE DONNÉES
    // ========================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setResponseHeaders(response);
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && !pathInfo.equals("/")) {
                // URL: /api/users/{id}
                getUserById(request, response, pathInfo);

            } else if (request.getParameter("matricule") != null) {
                // URL: /api/users?matricule=XXX
                getUserByMatricule(request, response);

            } else if (request.getParameter("email") != null) {
                // URL: /api/users?email=XXX
                getUserByEmail(request, response);

            } else if (request.getParameter("name") != null) {
                // URL: /api/users?name=XXX
                searchUsersByName(request, response);

            } else if (request.getParameter("grade") != null) {
                // URL: /api/users?grade=SENIOR
                getUsersByGrade(request, response);

            } else if (request.getParameter("role") != null) {
                // URL: /api/users?role=EMPLOYE
                getUsersByRole(request, response);

            } else if (request.getParameter("department") != null) {
                // URL: /api/users?department=1
                getUsersByDepartment(request, response);

            } else if (request.getParameter("position") != null) {
                // URL: /api/users?position=2
                getUsersByPosition(request, response);

            } else {
                // URL: /api/users (sans paramètres)
                getAllUsers(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Erreur serveur interne: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTHODE POST - CRÉATION
    // ========================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setResponseHeaders(response);

        try {
            String jsonBody = getRequestBody(request);

            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                sendErrorResponse(response, 400, "Corps de requête vide");
                return;
            }

            UserDTO userDTO = gson.fromJson(jsonBody, UserDTO.class);

            String validationError = validateUser(userDTO, true);
            if (validationError != null) {
                sendErrorResponse(response, 400, validationError);
                return;
            }

            // Vérification des doublons
            if (userDAO.findByMatricule(userDTO.getMatricule()).isPresent()) {
                sendErrorResponse(response, 409, "Ce matricule existe déjà");
                return;
            }

            if (userDAO.findByEmail(userDTO.getEmail()).isPresent()) {
                sendErrorResponse(response, 409, "Cet email existe déjà");
                return;
            }

            User user = userDTO.toEntity();

            // Assignation du département
            if (userDTO.getDepartmentId() != null) {
                Optional<Department> dept = departmentDAO.findById(userDTO.getDepartmentId());
                if (dept.isPresent()) {
                    user.setDepartment(dept.get());
                } else {
                    sendErrorResponse(response, 404, "Département introuvable");
                    return;
                }
            }

            // Assignation du poste
            if (userDTO.getPositionId() != null) {
                Optional<Position> pos = positionDAO.findById(userDTO.getPositionId());
                if (pos.isPresent()) {
                    user.setPosition(pos.get());
                } else {
                    sendErrorResponse(response, 404, "Poste introuvable");
                    return;
                }
            }

            User savedUser = userDAO.save(user);

            if (savedUser != null) {
                //  Inclure l'image dans la réponse POST
                UserDTO responseDTO = UserDTO.fromEntity(savedUser, true);
                response.setStatus(HttpServletResponse.SC_CREATED);
                sendSuccessResponse(response, responseDTO, "Employé créé avec succès");
            } else {
                sendErrorResponse(response, 500, "Erreur lors de la création");
            }

        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, 400, "JSON invalide: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Erreur serveur: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTHODE PUT - MISE À JOUR
    // ========================================

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setResponseHeaders(response);
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, 400, "ID manquant dans l'URL");
            return;
        }

        try {
            Integer id = Integer.parseInt(pathInfo.substring(1));

            Optional<User> existingUserOpt = userDAO.findById(id);
            if (!existingUserOpt.isPresent()) {
                sendErrorResponse(response, 404, "Employé introuvable");
                return;
            }

            User existingUser = existingUserOpt.get();

            String jsonBody = getRequestBody(request);
            UserDTO userDTO = gson.fromJson(jsonBody, UserDTO.class);

            String validationError = validateUser(userDTO, false);
            if (validationError != null) {
                sendErrorResponse(response, 400, validationError);
                return;
            }

            // Vérification des doublons
            if (userDTO.getMatricule() != null) {
                Optional<User> duplicate = userDAO.findByMatricule(userDTO.getMatricule());
                if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                    sendErrorResponse(response, 409, "Ce matricule existe déjà");
                    return;
                }
            }

            if (userDTO.getEmail() != null) {
                Optional<User> duplicate = userDAO.findByEmail(userDTO.getEmail());
                if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                    sendErrorResponse(response, 409, "Cet email existe déjà");
                    return;
                }
            }

            userDTO.updateEntity(existingUser);

            // Mise à jour des relations
            if (userDTO.getDepartmentId() != null) {
                Optional<Department> dept = departmentDAO.findById(userDTO.getDepartmentId());
                dept.ifPresent(existingUser::setDepartment);
            }

            if (userDTO.getPositionId() != null) {
                Optional<Position> pos = positionDAO.findById(userDTO.getPositionId());
                pos.ifPresent(existingUser::setPosition);
            }

            User updatedUser = userDAO.update(existingUser);

            if (updatedUser != null) {
                //  Inclure l'image dans la réponse PUT
                UserDTO responseDTO = UserDTO.fromEntity(updatedUser, true);
                sendSuccessResponse(response, responseDTO, "Employé mis à jour avec succès");
            } else {
                sendErrorResponse(response, 500, "Erreur lors de la mise à jour");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "ID invalide");
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, 400, "JSON invalide: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Erreur serveur: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTHODE DELETE - SUPPRESSION
    // ========================================

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setResponseHeaders(response);
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, 400, "ID manquant dans l'URL");
            return;
        }

        try {
            Integer id = Integer.parseInt(pathInfo.substring(1));

            if (!userDAO.exists(id)) {
                sendErrorResponse(response, 404, "Employé introuvable");
                return;
            }

            boolean deleted = userDAO.deleteById(id);

            if (deleted) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Employé supprimé avec succès");

                PrintWriter out = response.getWriter();
                out.print(gson.toJson(responseData));
                out.flush();
            } else {
                sendErrorResponse(response, 500, "Erreur lors de la suppression");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "ID invalide");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, 500, "Erreur serveur: " + e.getMessage());
        }
    }

    // ========================================
    // HANDLERS GET - MÉTHODES AUXILIAIRES
    // ========================================

    private void getUserById(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        try {
            Integer id = Integer.parseInt(pathInfo.substring(1));
            Optional<User> userOpt = userDAO.findById(id);

            if (userOpt.isPresent()) {
                //  Toujours inclure l'image pour GET by ID
                UserDTO dto = UserDTO.fromEntity(userOpt.get(), true);
                sendSuccessResponse(response, dto, null);
            } else {
                sendErrorResponse(response, 404, "Employé introuvable");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "ID invalide");
        }
    }

    private void getUserByMatricule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String matricule = request.getParameter("matricule");
        Optional<User> userOpt = userDAO.findByMatricule(matricule);

        if (userOpt.isPresent()) {
            UserDTO dto = UserDTO.fromEntity(userOpt.get(), true);
            sendSuccessResponse(response, dto, null);
        } else {
            sendErrorResponse(response, 404, "Employé introuvable");
        }
    }

    private void getUserByEmail(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String email = request.getParameter("email");
        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isPresent()) {
            UserDTO dto = UserDTO.fromEntity(userOpt.get(), true);
            sendSuccessResponse(response, dto, null);
        } else {
            sendErrorResponse(response, 404, "Employé introuvable");
        }
    }

    /**
     *  NOUVEAU : Recherche par nom ou prénom
     */
    private void searchUsersByName(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String name = request.getParameter("name");
        List<User> users = userDAO.findAll();

        // Filtrer par nom ou prénom (case insensitive)
        List<User> filtered = users.stream()
                .filter(u -> u.getLastName().toLowerCase().contains(name.toLowerCase()) ||
                        u.getFirstName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        boolean includeImage = "true".equalsIgnoreCase(request.getParameter("includeImage"));
        List<UserDTO> dtos = filtered.stream()
                .map(user -> UserDTO.fromEntity(user, includeImage))
                .collect(Collectors.toList());

        sendSuccessResponse(response, dtos, null);
    }

    private void getUsersByGrade(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String gradeStr = request.getParameter("grade");
        try {
            Grade grade = Grade.valueOf(gradeStr.toUpperCase());
            List<User> users = userDAO.findByGrade(grade);

            boolean includeImage = "true".equalsIgnoreCase(request.getParameter("includeImage"));
            List<UserDTO> dtos = users.stream()
                    .map(user -> UserDTO.fromEntity(user, includeImage))
                    .collect(Collectors.toList());

            sendSuccessResponse(response, dtos, null);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, 400, "Grade invalide. Valeurs possibles : JUNIOR, SENIOR, EXPERT");
        }
    }

    private void getUsersByRole(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String roleStr = request.getParameter("role");
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            List<User> users = userDAO.findByRole(role);

            boolean includeImage = "true".equalsIgnoreCase(request.getParameter("includeImage"));
            List<UserDTO> dtos = users.stream()
                    .map(user -> UserDTO.fromEntity(user, includeImage))
                    .collect(Collectors.toList());

            sendSuccessResponse(response, dtos, null);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, 400, "Rôle invalide. Valeurs possibles : ADMINISTRATEUR, CHEF_DEPARTEMENT, CHEF_PROJET, EMPLOYE");
        }
    }

    private void getUsersByDepartment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Integer deptId = Integer.parseInt(request.getParameter("department"));
            List<User> users = userDAO.findByDepartment(deptId);

            boolean includeImage = "true".equalsIgnoreCase(request.getParameter("includeImage"));
            List<UserDTO> dtos = users.stream()
                    .map(user -> UserDTO.fromEntity(user, includeImage))
                    .collect(Collectors.toList());

            sendSuccessResponse(response, dtos, null);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "ID de département invalide");
        }
    }

    private void getUsersByPosition(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Integer posId = Integer.parseInt(request.getParameter("position"));
            List<User> users = userDAO.findByPosition(posId);

            boolean includeImage = "true".equalsIgnoreCase(request.getParameter("includeImage"));
            List<UserDTO> dtos = users.stream()
                    .map(user -> UserDTO.fromEntity(user, includeImage))
                    .collect(Collectors.toList());

            sendSuccessResponse(response, dtos, null);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "ID de poste invalide");
        }
    }

    /**
     *  MODIFIÉ : Support du paramètre ?includeImage=true
     */
    private void getAllUsers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Lire le paramètre optionnel includeImage
        boolean includeImage = "true".equalsIgnoreCase(request.getParameter("includeImage"));

        List<User> users = userDAO.findAll();

        // Convertir avec ou sans images selon le paramètre
        List<UserDTO> dtos = users.stream()
                .map(user -> UserDTO.fromEntity(user, includeImage))
                .collect(Collectors.toList());

        sendSuccessResponse(response, dtos, null);
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    private void setResponseHeaders(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private String validateUser(UserDTO user, boolean isCreation) {
        if (isCreation) {
            if (user.getMatricule() == null || user.getMatricule().trim().isEmpty()) {
                return "Le matricule est obligatoire";
            }
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                return "Le nom est obligatoire";
            }
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                return "Le prénom est obligatoire";
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return "L'email est obligatoire";
            }
        }

        if (user.getEmail() != null && !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Format d'email invalide";
        }

        if (user.getMatricule() != null && user.getMatricule().length() > 20) {
            return "Le matricule ne peut pas dépasser 20 caractères";
        }

        return null;
    }

    private void sendSuccessResponse(HttpServletResponse response, Object data, String message)
            throws IOException {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        if (message != null) {
            responseData.put("message", message);
        }
        responseData.put("data", data);

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(responseData));
        out.flush();
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("success", false);
        errorData.put("error", message);
        errorData.put("timestamp", new Date().toString());

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(errorData));
        out.flush();
    }

    @Override
    public void destroy() {
        System.out.println("UserServlet détruit");
        super.destroy();
    }
}