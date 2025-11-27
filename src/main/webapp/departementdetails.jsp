<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.projetjee.model.Department" %>
<%@ page import="fr.projetjee.model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="fr.projetjee.enums.Role" %>

<%
    Department department = (Department) request.getAttribute("department");
    List<User> departmentEmployees = (List<User>) request.getAttribute("departmentEmployees");
    User departmentHead = (User) request.getAttribute("departmentHead");
    User currentUser = (User) session.getAttribute("currentUser");

    boolean showActions = false;
    if (currentUser != null) {
        String deptCode = currentUser.getDepartment() != null ? currentUser.getDepartment().getCode() : "";
        if (currentUser.getRole() == Role.ADMINISTRATEUR ||
                currentUser.getRole() == Role.CHEF_DEPARTEMENT ||
                (currentUser.getRole() == Role.EMPLOYE && "RH".equalsIgnoreCase(deptCode))) {
            showActions = true;
        }
    }
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <link href="assets/css/projectdetails.css" rel="stylesheet" />
    <title>Détails du département</title>
    <style>
        .department-details {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            color: white;
        }

        .btn-back {
            background: linear-gradient(135deg, #3b82f6, #06b6d4);
            color: white;
            border: none;
            padding: 12px 20px;
            border-radius: 10px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 600;
            margin-bottom: 30px;
            box-shadow: 0 8px 22px rgba(59,130,246,.35);
            transition: filter 0.2s ease-in-out;
        }

        .btn-back:hover {
            filter: brightness(1.1);
        }

        .department-header {
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            border: 1px solid rgba(255, 255, 255, 0.2);
        }

        .department-info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            margin-top: 20px;
        }

        .info-card {
            background: rgba(255, 255, 255, 0.05);
            padding: 20px;
            border-radius: 10px;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .department-manager-info {
            display: flex;
            align-items: center;
            gap: 20px;
            margin-top: 15px;
        }

        .profile-pic {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid rgba(59, 130, 246, 0.5);
        }

        .employees-assigned {
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            border-radius: 15px;
            padding: 30px;
            border: 1px solid rgba(255, 255, 255, 0.2);
        }

        .project-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 10px;
            overflow: hidden;
        }

        .project-table th {
            background: rgba(59, 130, 246, 0.3);
            padding: 15px;
            text-align: left;
            font-weight: 600;
        }

        .project-table td {
            padding: 12px 15px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }

        .project-table tr:hover {
            background: rgba(255, 255, 255, 0.05);
        }

        .btn-assign {
            background: linear-gradient(135deg, #10b981, #059669);
            color: white;
            border: none;
            padding: 12px 25px;
            border-radius: 10px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 600;
            margin-top: 20px;
            box-shadow: 0 8px 22px rgba(16, 185, 129, .35);
            transition: filter 0.2s ease-in-out;
        }

        .btn-assign:hover {
            filter: brightness(1.1);
        }

        .action-buttons {
            display: flex;
            gap: 15px;
            margin-top: 20px;
        }

        .btn-edit {
            background: linear-gradient(135deg, #f59e0b, #d97706);
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.9rem;
            font-weight: 600;
        }

        .btn-delete {
            background: linear-gradient(135deg, #ef4444, #dc2626);
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.9rem;
            font-weight: 600;
        }

        .no-data {
            text-align: center;
            padding: 40px;
            color: rgba(255, 255, 255, 0.7);
            font-style: italic;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: rgba(255, 255, 255, 0.1);
            padding: 20px;
            border-radius: 10px;
            text-align: center;
            border: 1px solid rgba(255, 255, 255, 0.2);
        }

        .stat-number {
            font-size: 2.5rem;
            font-weight: bold;
            color: #3b82f6;
            margin-bottom: 5px;
        }

        .stat-label {
            font-size: 0.9rem;
            color: rgba(255, 255, 255, 0.8);
        }
    </style>
</head>
<body>
<div class="bg"></div>

<div class="department-details">
    <!-- Bouton Retour -->
    <button class="btn-back" onclick="window.location.href='departments'">
        ← Retour à la liste des départements
    </button>

    <% if (department != null) { %>
    <!-- En-tête du département -->
    <div class="department-header">
        <h2>Détails du département : <%= department.getName() %></h2>

        <!-- Statistiques -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number"><%= departmentEmployees != null ? departmentEmployees.size() : 0 %></div>
                <div class="stat-label">Employés</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= department.getCode() %></div>
                <div class="stat-label">Code</div>
            </div>
        </div>

        <!-- Informations détaillées -->
        <div class="department-info-grid">
            <div class="info-card">
                <h3>Informations générales</h3>
                <p><strong>Nom :</strong> <%= department.getName() %></p>
                <p><strong>Code :</strong> <%= department.getCode() %></p>
                <p><strong>ID :</strong> <%= department.getId() %></p>
            </div>

            <div class="info-card">
                <h3>Description</h3>
                <p><%= department.getDescription() != null && !department.getDescription().isEmpty() ?
                        department.getDescription() : "Aucune description disponible" %></p>
            </div>
        </div>

        <!-- Chef de département -->
        <div class="info-card">
            <h3>Chef de département</h3>
            <% if (departmentHead != null) { %>
            <div class="department-manager-info">
                <img src="https://via.placeholder.com/150" alt="Photo du Chef de Département" class="profile-pic">
                <div>
                    <p><strong><%= departmentHead.getFirstName() %> <%= departmentHead.getLastName() %></strong></p>
                    <p>Email: <%= departmentHead.getEmail() %></p>
                    <p>Matricule: <%= departmentHead.getMatricule() %></p>
                    <p>Téléphone: <%= departmentHead.getPhone() != null ? departmentHead.getPhone() : "Non renseigné" %></p>
                </div>
            </div>
            <% } else { %>
            <p class="no-data">Aucun chef de département assigné</p>
            <% } %>
        </div>

        <!-- Boutons d'action -->
        <% if (showActions) { %>
        <div class="action-buttons">
            <button class="btn-edit" onclick="toggleUpdateModal()">Modifier le département</button>
            <form method="post" action="departments" style="display: inline;">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <input type="hidden" name="id" value="<%= department.getId() %>">
                <button type="submit" class="btn-delete" name="action" value="delete"
                        onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce département ?')">
                    Supprimer le département
                </button>
            </form>
        </div>
        <% } %>
    </div>

    <!-- Liste des employés affectés -->
    <div class="employees-assigned">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3>Employés du département (<%= departmentEmployees != null ? departmentEmployees.size() : 0 %>)</h3>
            <% if (showActions) { %>
            <button class="btn-assign" onclick="toggleAssignModal()">Assigner un employé</button>
            <% } %>
        </div>

        <% if (departmentEmployees != null && !departmentEmployees.isEmpty()) { %>
        <table class="project-table">
            <thead>
            <tr>
                <th>Matricule</th>
                <th>Nom</th>
                <th>Prénom</th>
                <th>Email</th>
                <th>Téléphone</th>
                <th>Poste</th>
            </tr>
            </thead>
            <tbody>
            <% for (User employee : departmentEmployees) { %>
            <tr>
                <td><%= employee.getMatricule() %></td>
                <td><%= employee.getLastName() %></td>
                <td><%= employee.getFirstName() %></td>
                <td><%= employee.getEmail() %></td>
                <td><%= employee.getPhone() != null ? employee.getPhone() : "N/A" %></td>
                <td><%= employee.getPosition() != null ? employee.getPosition() : "N/A" %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } else { %>
        <div class="no-data">
            Aucun employé n'est actuellement affecté à ce département
        </div>
        <% } %>
    </div>

    <% } else { %>
    <!-- Message si département non trouvé -->
    <div class="department-header">
        <h2>Département non trouvé</h2>
        <p>Le département que vous recherchez n'existe pas ou a été supprimé.</p>
    </div>
    <% } %>
</div>

<!-- Modale pour assigner un employé -->
<% if (showActions && department != null) { %>
<div id="modalAssign" class="modal-container" style="display: none;">
    <div class="modal-box">
        <h3>Assigner un employé au département</h3>
        <form method="post" action="departments">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
            <input type="hidden" name="departmentId" value="<%= department.getId() %>">

            <select name="registrationNumber" required
                    style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%; margin-bottom: 10px;">
                <option value="">Sélectionner un employé</option>
                <%
                    List<User> allEmployees = (List<User>) request.getAttribute("allEmployees");
                    if (allEmployees != null) {
                        for (User emp : allEmployees) {
                %>
                <option value="<%= emp.getMatricule() %>">
                    <%= emp.getMatricule() %> - <%= emp.getFirstName() %> <%= emp.getLastName() %>
                </option>
                <% } } %>
            </select>

            <button class="btn-save" name="action" value="assign">Assigner</button>
            <button type="button" class="btn-cancel" onclick="toggleAssignModal()">Annuler</button>
        </form>
    </div>
</div>
<% } %>

<script>
    // Fonctions pour gérer les modales
    function toggleAssignModal() {
        const modal = document.getElementById('modalAssign');
        modal.style.display = modal.style.display === 'flex' ? 'none' : 'flex';
    }

    function toggleUpdateModal() {
        // Rediriger vers la page departments avec ouverture de la modale de modification
        window.location.href = 'departments?action=edit&id=<%= department != null ? department.getId() : "" %>';
    }

    // Fermer la modale en cliquant à l'extérieur
    document.addEventListener('click', function(event) {
        const modal = document.getElementById('modalAssign');
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });

    // Styles pour les modales (identique à departments.jsp)
    const style = document.createElement('style');
    style.textContent = `
            .modal-container {
                display: none;
                position: fixed;
                inset: 0;
                background: rgba(0,0,0,0.45);
                backdrop-filter: blur(6px);
                -webkit-backdrop-filter: blur(6px);
                z-index: 999;
                justify-content: center;
                align-items: center;
            }
            .modal-box {
                width: 520px;
                padding: 30px;
                border-radius: 22px;
                background: rgba(255,255,255,0.15);
                border: 1px solid rgba(255,255,255,0.28);
                backdrop-filter: blur(18px);
                -webkit-backdrop-filter: blur(18px);
                box-shadow: 0 25px 45px rgba(0,0,0,0.35);
                color: white;
            }
            .btn-save {
                width: 100%;
                padding: 12px;
                margin-top: 10px;
                font-size: 1.1rem;
                font-weight: 600;
                border: none;
                border-radius: 12px;
                cursor: pointer;
                background: linear-gradient(135deg, #3b82f6, #06b6d4);
                color: white;
            }
            .btn-cancel {
                width: 100%;
                padding: 12px;
                margin-top: 12px;
                font-size: 1.1rem;
                font-weight: 600;
                border: none;
                border-radius: 12px;
                cursor: pointer;
                background: #ef4444;
                color: white;
            }
        `;
    document.head.appendChild(style);
</script>
</body>
</html>