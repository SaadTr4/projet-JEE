<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.Position" %>
<%@ page import="fr.projetjee.model.User" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="fr.projetjee.enums.Role" %>

<%
    List<Position> positions = (List<Position>) request.getAttribute("positions");
    User currentUser = (User) session.getAttribute("currentUser");
    String username = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Invité";
    boolean showAddButton = false;
    boolean showActions = false;

    if (currentUser != null) {
        String deptCode = currentUser.getDepartment() != null ? currentUser.getDepartment().getCode() : "";

        if (currentUser.getRole() == Role.ADMINISTRATEUR || currentUser.getRole() == Role.CHEF_DEPARTEMENT) {
            showAddButton = true;
            showActions = true;
        } else if (currentUser.getRole() == Role.EMPLOYE && "RH".equalsIgnoreCase(deptCode)) {
            showAddButton = true;
            showActions = true;
        }
    }
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Postes - Gestion RH</title>
    <link href="assets/css/dashboard.css" rel="stylesheet">
    <style>
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

        .modal-box h3 {
            margin-top: 0;
            margin-bottom: 18px;
            font-size: 1.6rem;
            font-weight: 700;
        }

        .modal-box input,
        .modal-box textarea,
        .modal-box select {
            width: 100%;
            background: rgba(255, 255, 255, 0.23);
            color: #ffffff;
            border: 1px solid rgba(255, 255, 255, 0.45);
            padding: 12px;
            border-radius: 12px;
            margin-bottom: 14px;
            font-size: 1.05rem;
            backdrop-filter: blur(8px);
        }

        .modal-box input::placeholder,
        .modal-box textarea::placeholder {
            color: rgba(240, 240, 240, 0.85);
        }

        .modal-box select option {
            background: rgba(20,20,20,0.9);
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

        .action-buttons {
            display: flex;
            gap: 8px;
            justify-content: center;
        }

        .btn-action {
            font-size: 0.85rem;
            padding: 6px 10px;
            border-radius: 8px;
            font-weight: 600;
            box-shadow: 0 8px 22px rgba(59,130,246,.35);
            transition: filter 0.2s ease-in-out;
            border: none;
            cursor: pointer;
        }

        .btn-modify {
            background: linear-gradient(135deg, #3b82f6, #06b6d4);
            color: white;
        }

        .btn-delete {
            background: #ef4444;
            color: white;
        }
    </style>
</head>
<body>
<div class="bg"></div>

<div class="app-shell">
    <!-- SIDEBAR -->
    <aside class="sidebar">
        <nav class="side-nav">
            <a class="side-link" href="dashboard.jsp">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M11 3 2 9v12h7v-7h6v7h7V9z"/></svg>
                <span>Tableau de bord</span>
            </a>
            <a class="side-link" href="user">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm-7 9a7 7 0 0 1 14 0Z"/></svg>
                <span>Employés</span>
            </a>
            <a class="side-link active" href="positions">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 3v18M4 9l4-4 4 4m0 0l4-4 4 4" /></svg>
                <span>Postes</span>
            </a>
            <a class="side-link" href="projects">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 3h18v4H3Zm0 7h18v4H3Zm0 7h18v4H3Z"/></svg>
                <span>Projets</span>
            </a>
            <a class="side-link" href="departments">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 13h8V3H3Zm10 8h8V3h-8ZM3 21h8v-6H3Z"/></svg>
                <span>Départements</span>
            </a>
            <a class="side-link" href="payslips">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M6 2h9l5 5v15a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm8 1.5V8h4.5ZM8 8h4v2H8Zm0 4h8v2H8Zm0 4h8v2H8Z"/></svg>
                <span>Fiches de paie</span>
            </a>
        </nav>
    </aside>

    <div class="main">
        <header class="app-header">
            <div class="brand">
                <span class="brand-dot"></span>
                <span class="brand-text">Gestion RH</span>
            </div>
            <a href="logout" class="welcome-logout">Se déconnecter</a>
        </header>

        <main class="page">
            <div class="dashboard-container">
                <div class="welcome-card">
                    <div class="welcome-left">
                        <div>
                            <h2 class="welcome-title">Liste des postes</h2>
                            <p class="welcome-sub">Connecté en tant que <%= username %></p>
                        </div>
                    </div>
                    <% if (showAddButton) { %>
                    <button style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem;"
                            onclick="toggleAddModal(true)">+ Ajouter un poste</button>
                    <% } %>
                </div>

                <!-- Affichage des erreurs/succès -->
                <% String error = (String) request.getAttribute("error"); %>
                <% if (error != null) { %>
                <div style="background: rgba(239, 68, 68, 0.2); border: 1px solid rgba(239, 68, 68, 0.5); color: #fca5a5; padding: 12px; border-radius: 10px; margin-bottom: 16px; backdrop-filter: blur(8px);">
                    <%= error %>
                </div>
                <% } %>

                <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
                    <table style="width:100%; border-collapse:collapse; color:#fff; border-spacing: 0 12px;">
                        <thead style="background:rgba(255,255,255,.15); text-align: center;">
                        <tr>
                            <th style="padding:10px;">ID</th>
                            <th>Nom du poste</th>
                            <th>Description</th>
                            <th>Nombre d'employés</th>
                            <% if (showActions) { %><th>Actions</th><% } %>
                        </tr>
                        </thead>
                        <tbody>
                        <% if (positions != null && !positions.isEmpty()) {
                            for (Position p : positions) {
                        %>
                        <tr style="border-bottom:1px solid rgba(255,255,255,.2); text-align: center;">
                            <td style="padding:10px;"><%= p.getId() %></td>
                            <td><%= p.getName() %></td>
                            <td><%= p.getDescription() != null ? (p.getDescription().length() > 50 ? p.getDescription().substring(0, 50) + "..." : p.getDescription()) : "" %></td>
                            <td><%= p.getUsers() != null ? p.getUsers().size() : 0 %></td>
                            <% if (showActions) { %>
                            <td class="action-buttons">
                                <button type="button" class="btn-action btn-modify"
                                        onclick='toggleUpdateModal({
                                                id:<%= p.getId() %>,
                                                name:"<%= StringEscapeUtils.escapeEcmaScript(p.getName()) %>",
                                                description:"<%= p.getDescription() != null ? StringEscapeUtils.escapeEcmaScript(p.getDescription()) : "" %>"
                                                })'>Modifier</button>

                                <form method="post" action="positions" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                    <input type="hidden" name="id" value="<%= p.getId() %>">
                                    <button type="submit" class="btn-action btn-delete" name="action" value="delete"
                                            onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce poste ?')">Supprimer</button>
                                </form>
                            </td>
                            <% } %>
                        </tr>
                        <% }
                        } else { %>
                        <tr>
                            <td colspan="<%= showActions ? 5 : 4 %>" style="text-align: center; padding: 20px; color: rgba(255,255,255,0.7);">
                                Aucun poste trouvé
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Modal Ajout Poste -->
<div id="modalAdd" class="modal-container">
    <div class="modal-box">
        <h3 style="color: #fff;">Ajouter un poste</h3>
        <form method="post" action="positions">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
            <input type="hidden" name="action" value="create">

            <label for="name" style="color: white; font-weight: bold; margin-bottom: 5px;">Nom du poste *</label>
            <input id="name" name="name" class="input" placeholder="Nom du poste" required maxlength="100"
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <label for="description" style="color: white; font-weight: bold; margin-bottom: 5px;">Description</label>
            <textarea id="description" name="description" class="input" placeholder="Description (optionnel)"
                      style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; height: 80px; margin-bottom: 10px;"></textarea>

            <button class="btn-save" type="submit">Enregistrer</button>
            <button type="button" class="btn-cancel" onclick="toggleAddModal(false)">Annuler</button>
        </form>
    </div>
</div>

<!-- Modal Modification Poste -->
<div id="modalUpdate" class="modal-container">
    <div class="modal-box">
        <h3 style="color: #fff;">Modifier le poste</h3>
        <form id="updateForm" method="post" action="positions">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" id="updateId">

            <label for="updateName" style="color: white; font-weight: bold; margin-bottom: 5px;">Nom du poste *</label>
            <input id="updateName" name="name" class="input" placeholder="Nom du poste" required maxlength="100"
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <label for="updateDescription" style="color: white; font-weight: bold; margin-bottom: 5px;">Description</label>
            <textarea id="updateDescription" name="description" class="input" placeholder="Description (optionnel)"
                      style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; height: 80px; margin-bottom: 10px;"></textarea>

            <button class="btn-save" type="submit">Mettre à jour</button>
            <button type="button" class="btn-cancel" onclick="toggleUpdateModal(null)">Annuler</button>
        </form>
    </div>
</div>

<script>
    // Fonctions pour gérer les modales
    function toggleAddModal(show) {
        document.getElementById('modalAdd').style.display = show ? 'flex' : 'none';
        if (show) {
            // Réinitialiser le formulaire d'ajout
            document.querySelector('#modalAdd form').reset();
        }
    }

    function toggleUpdateModal(position) {
        const modal = document.getElementById('modalUpdate');
        const form = document.getElementById('updateForm');

        if (position) {
            document.getElementById('updateId').value = position.id;
            document.getElementById('updateName').value = position.name;
            document.getElementById('updateDescription').value = position.description || '';
            modal.style.display = 'flex';
        } else {
            modal.style.display = 'none';
        }
    }

    // Fermer les modales en cliquant à l'extérieur
    document.addEventListener('click', function(event) {
        const modals = document.querySelectorAll('.modal-container');
        modals.forEach(modal => {
            if (event.target === modal) {
                modal.style.display = 'none';
            }
        });
    });

    // Validation côté client pour les formulaires
    document.addEventListener('DOMContentLoaded', function() {
        const forms = document.querySelectorAll('form');
        forms.forEach(form => {
            form.addEventListener('submit', function(e) {
                const requiredFields = form.querySelectorAll('[required]');
                let valid = true;

                requiredFields.forEach(field => {
                    if (!field.value.trim()) {
                        valid = false;
                        field.style.borderColor = '#ef4444';
                    } else {
                        field.style.borderColor = 'rgba(255, 255, 255, 0.3)';
                    }
                });

                if (!valid) {
                    e.preventDefault();
                    alert('Veuillez remplir tous les champs obligatoires.');
                }
            });
        });
    });
</script>

</body>
</html>