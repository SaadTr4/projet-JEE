<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.projetjee.model.Project, fr.projetjee.model.User, java.util.Set" %>
<%
    // Le projet est injecté depuis ProjectServlet
    Project project = (Project) request.getAttribute("project");
    if (project == null) {
        System.out.println("<p>Projet introuvable.</p>");
        return;
    }

    Set<User> usersAssigned = request.getAttribute("usersAssigned") != null ?
            (Set<User>) request.getAttribute("usersAssigned") : null;
    User manager = project.getProjectManager();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <link href="<%= request.getContextPath() %>/assets/css/projectdetails.css" rel="stylesheet" />
    <title>Détails du projet</title>
</head>
<body>
    <div class="bg"></div>

    <div class="project-details">
        <!-- Bouton Retour -->
        <button class="btn-back" onclick="window.location.href='<%= request.getContextPath() %>/projects'">
            Retour à la liste des projets</button>

        <!-- Titre du projet -->
        <h2>Détails du projet : <%= project.getName() %></h2>

        <!-- Description du projet -->
        <p class="project-description">
            <strong>Description :</strong>
            <%= project.getDescription() != null ? project.getDescription() : "Aucune description fournie." %>
        </p>

        <!-- Chef de projet avec image -->
        <div class="project-manager-info">
            <% if (project.getProjectManager().getImage() != null) { %>
            <img src="?action=image"
                 class="profile-pic"
                 alt="Photo <%= project.getProjectManager().getFullName() %>">
            <% } else { %>
            <div class="no-pic">👤</div>
            <% } %>
            <p class="project-manager">
                <strong>Chef de projet :</strong> <%= project.getProjectManager().getFullName() %>
            </p>
        </div>

        <!-- Statut -->
        <p class="project-status">
            <strong>Statut :</strong> <%= project.getStatus() != null ? project.getStatus().getDisplayName() : "En cours" %>
        </p>

        <!-- Liste des employés affectés -->
        <div class="employees-assigned">
            <h3>Employés affectés au projet</h3>
            <table class="project-table">
                <thead>
                <tr>
                    <% if ((Boolean) request.getAttribute("canViewMatricule")) { %>
                    <th>Matricule</th>
                    <% } %>
                    <th>Nom</th>
                    <th>Email</th>
                    <% if ((Boolean) request.getAttribute("canManageMembers")) { %>
                    <th>Action</th>
                    <% } %>
                </tr>
                </thead>
                <tbody>
                <%
                    if (usersAssigned != null && !usersAssigned.isEmpty()) {
                        boolean canManageMembers = (Boolean) request.getAttribute("canManageMembers");
                        boolean canViewMatricule = (Boolean) request.getAttribute("canViewMatricule");

                        for (User u : usersAssigned) {
                            // Vérifier si c'est le chef de projet
                            boolean isProjectManager = manager != null &&
                                    manager.getId().equals(u.getId());
                %>
                <tr>
                    <% if (canViewMatricule) { %>
                    <td><%= u.getMatricule() %></td>
                    <% } %>
                    <td><%= u.getFullName() %></td>
                    <td><%= u.getEmail() %></td>
                    <% if (canManageMembers) { %>
                    <td>
                        <% if (!isProjectManager) { %>
                        <form method="post" action="project-detail" style="display:inline;">
                            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                            <input type="hidden" name="projectId" value="<%= project.getId() %>">
                            <input type="hidden" name="userMatricule" value="<%= u.getMatricule() %>">
                            <input type="hidden" name="action" value="remove">
                            <button type="submit" class="btn-danger"
                                    onclick="return confirm('Retirer <%= u.getFullName() %> du projet ?')">
                                Retirer
                            </button>
                        </form>
                        <% } else { %>
                        <span style="color: rgba(255,255,255,0.6); font-style: italic;">Chef de projet</span>
                        <% } %>
                    </td>
                    <% } %>
                </tr>
                <%
                    }
                } else {
                %>
                <tr>
                    <td colspan="<%= (Boolean) request.getAttribute("canViewMatricule") ? "4" : "3" %>">
                        Aucun employé assigné pour le moment.
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <!-- Bouton pour assigner un nouvel employé -->
        <% if ((Boolean) request.getAttribute("canManageMembers")) { %>
        <button type="button" class="btn-assign" onclick="openAssignModal()">Assigner</button>
        <% } %>
    </div>

    <div id="assignModal" class="modal-container">
        <div class="modal-box">
            <h3 class="modal-title">Assigner un ou plusieurs employés au projet</h3>
            <form method="post" action="project-detail" id="assignForm">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <input type="hidden" name="projectId" value="<%= project.getId() %>">
                <input type="hidden" name="action" value="assign">

                <!-- Checkbox pour activer le mode multiple -->
                <div style="margin-bottom: 15px;">
                    <label style="color: white; font-weight: bold; display: flex; align-items: center; gap: 8px; cursor: pointer;">
                        <input type="checkbox" id="multipleMode" onchange="toggleMultipleMode()"
                               style="width: 18px; height: 18px; cursor: pointer;">
                        Assigner plusieurs employés
                    </label>
                </div>

                <!-- Zone pour un seul employé -->
                <div id="singleEmployeeZone">
                    <label style="color: white; font-weight: bold; margin-bottom: 8px; display: block;">
                        Sélectionner un employé *
                    </label>
                    <input type="text"
                           name="singleEmployee"
                           id="singleEmployee"
                           list="employeesList"
                           class="modal-select"
                           placeholder="Matricule de l'employé"
                           oninput="validateSingleEmployee(this)"
                           style="background: rgba(255, 255, 255, 0.23); color: #ffffff; border: 1px solid rgba(255, 255, 255, 0.45); padding: 12px; border-radius: 12px; width: 95%; font-size: 1.05rem; backdrop-filter: blur(8px);">
                    <span id="singleError" style="color: #ef4444; font-size: 0.9rem; display: none; margin-top: 5px;">Employé invalide</span>
                </div>

                <!-- Zone pour plusieurs employés (cachée par défaut) -->
                <div id="multipleEmployeesZone" style="display: none;">
                    <label style="color: white; font-weight: bold; margin-bottom: 8px; display: block;">
                        Ajouter des employés *
                    </label>

                    <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                        <input type="text"
                               id="employeeInput"
                               list="employeesList"
                               class="modal-select"
                               placeholder="Matricule de l'employé"
                               style="flex: 1; background: rgba(255, 255, 255, 0.23); color: #ffffff; border: 1px solid rgba(255, 255, 255, 0.45); padding: 12px; border-radius: 12px; font-size: 1.05rem; backdrop-filter: blur(8px);">
                        <button type="button"
                                onclick="addEmployee()"
                                style="background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 12px 20px; border: none; border-radius: 12px; cursor: pointer; font-weight: 600;">
                            Ajouter
                        </button>
                    </div>
                    <span id="multipleError" style="color: #ef4444; font-size: 0.9rem; display: none; margin-top: 5px;">Employé invalide ou déjà ajouté</span>

                    <!-- Liste des employés ajoutés -->
                    <div id="employeeList" style="margin-top: 15px; max-height: 200px; overflow-y: auto;"></div>
                    <input type="hidden" name="multipleEmployees" id="multipleEmployeesInput">
                </div>

                <datalist id="employeesList">
                    <%
                        // Récupérer tous les employés disponibles depuis le servlet
                        java.util.List<User> availableEmployees =
                                (java.util.List<User>) request.getAttribute("availableEmployees");
                        if (availableEmployees != null) {
                            for (User emp : availableEmployees) {
                    %>
                    <option value="<%= emp.getMatricule() %>"><%= emp.getMatricule() %> - <%= emp.getFullName() %></option>
                    <%
                            }
                        }
                    %>
                </datalist>

                <div class="modal-buttons">
                    <button type="submit" class="btn-modal-save" id="submitBtn">Assigner</button>
                    <button type="button" class="btn-modal-cancel" onclick="closeAssignModal()">Annuler</button>
                </div>
            </form>
        </div>
    </div>
    <script src="<%= request.getContextPath() %>/assets/js/app.js"></script>
</body>
</html>
