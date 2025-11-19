<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.Project" %>
<%@ page import="fr.projetjee.model.User" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="fr.projetjee.enums.Role" %>


<%
    List<Project> projects = (List<Project>) request.getAttribute("projects");
    List<User> chefs = (List<User>) request.getAttribute("chefs");
    List<Project> allProjects = (List<Project>) request.getAttribute("allProjects");

    User currentUser = (User) session.getAttribute("currentUser");
    String username = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Invité";
    boolean showFilter = false;
    boolean showAddButton = false;
    boolean showActions = false;

    if (currentUser != null) {
        String deptCode = currentUser.getDepartment() != null ? currentUser.getDepartment().getCode() : "";

        if (currentUser.getRole() == Role.ADMINISTRATEUR || currentUser.getRole() == Role.CHEF_DEPARTEMENT) {
            showFilter = true;
            showAddButton = true;
            showActions = true;
        } else if (currentUser.getRole() == Role.EMPLOYE && "RH".equalsIgnoreCase(deptCode)) {
            showFilter = true;
            showAddButton = true;
            showActions = true;
        } else if (currentUser.getRole() == Role.CHEF_PROJET) {
            showActions = true;
        }
        // Other roles have no special permissions (classic employee or no connection)
    }
%>
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Projets - Gestion RH</title>
    <link href="assets/css/dashboard.css" rel="stylesheet">
    <style>

        /* ----------------------------------------------------
           STYLE MODALE GLASSMORPHISM (effet comme les inputs)
        ----------------------------------------------------- */

        /* Fond flou derrière la modale */
        .modal-container {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(0,0,0,0.45);         /* léger fond foncé transparent */
            backdrop-filter: blur(6px);          /* flou de l'arrière-plan */
            -webkit-backdrop-filter: blur(6px);
            z-index: 999;
            justify-content: center;
            align-items: center;
        }

        /* Boîte modale glassmorphism */
        .modal-box {
            width: 520px;
            padding: 30px;
            border-radius: 22px;

            background: rgba(255,255,255,0.15);  /* ⚡ même style que les inputs */
            border: 1px solid rgba(255,255,255,0.28);

            backdrop-filter: blur(18px);         /* flou interne */
            -webkit-backdrop-filter: blur(18px);

            box-shadow: 0 25px 45px rgba(0,0,0,0.35);
            color: white;
        }

        /* Titre modale */
        .modal-box h3 {
            margin-top: 0;
            margin-bottom: 18px;
            font-size: 1.6rem;
            font-weight: 700;
        }

        /* Champs */
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

        /* Placeholder */
        .modal-box input::placeholder,
        .modal-box textarea::placeholder {
            color: rgba(240, 240, 240, 0.85);
        }

        /* Options du select */
        .modal-box select option {
            background: rgba(20,20,20,0.9);
            color: white;
        }
        /* ----- Style des champs du FILTRE de projets (Nom, Chef, Statut) ----- */
        .filter-input,
        .filter-select {
            background: rgba(255, 255, 255, 0.25);
            color: #fff;
            border: 1px solid rgba(255, 255, 255, 0.35);
            padding: 10px;
            border-radius: 12px;
            font-size: 1rem;
            backdrop-filter: blur(10px);
        }

        .filter-input::placeholder {
            color: rgba(255, 255, 255, 0.85);
        }

        .filter-select option {
            background: rgba(40,40,40,0.95);
            color: #fff;
        }


        /* Boutons */
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


    </style>


</head>
<body>
<div class="bg"></div>
<datalist id="chefsList">
    <% if (chefs != null) { for (User chef : chefs) { %>
    <option value="<%= chef.getMatricule() %>"><%= chef.getFullName() %></option>
    <% } } %>
</datalist>

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
            <a class="side-link active" href="projects">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 3h18v4H3Zm0 7h18v4H3Zm0 7h18v4H3Z"/></svg>
                <span>Projets</span>
            </a>
            <a class="side-link" href="departments.jsp">
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
                            <h2 class="welcome-title">Liste des projets</h2>
                            <p class="welcome-sub">Connecté en tant que <%= username %></p>
                        </div>
                    </div>
                    <% if (showAddButton) { %>
                    <button style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out;" onclick="toggleAddModal(true)">+ Ajouter</button>

                    <% } %>
                </div>

                <% if (showFilter) { %>
                <form method="post" action="projects" style="margin-bottom:16px; display:flex; gap:10px; flex-wrap:wrap; align-items:center;">
                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">

                    <!-- Nom du projet -->
                    <input name="name" class="filter-input"
                           list="projectsList"
                           placeholder="Nom du projet"
                           value="<%= request.getAttribute("filter_name") %>"
                           oninput="checkProjectValid(this)"
                           style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 200px;">

                    <datalist id="projectsList">
                        <% if(projects !=null) {for (Project p : allProjects) { %>
                        <option value="<%= p.getName() %>"></option>
                        <% } } %>
                    </datalist>

                    <!-- Chef de projet -->
                    <input name="manager" class="filter-input"
                           list="chefsList"
                           placeholder="Chef de projet"
                           value="<%= request.getAttribute("filter_manager") %>"
                           oninput="checkManagerValid(this)" data-required="false"
                           style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 200px;">

                    <!-- Statut -->
                    <select name="status" class="filter-select" style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 150px;">
                        <option value="">Tous</option>
                        <option value="IN_PROGRESS" <%= "IN_PROGRESS".equals(request.getAttribute("filter_status")) ? "selected" : "" %>>En cours</option>
                        <option value="COMPLETED" <%= "COMPLETED".equals(request.getAttribute("filter_status")) ? "selected" : "" %>>Terminé</option>
                        <option value="CANCELLED" <%= "CANCELLED".equals(request.getAttribute("filter_status")) ? "selected" : "" %>>Annulé</option>
                        <option value="PLANNED" <%= "PLANNED".equals(request.getAttribute("filter_status")) ? "selected" : "" %>>Planifié</option>
                    </select>

                    <button type="submit" class="welcome-logout" name="action" value="filter" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out;">Filtrer</button>
                    <button type="submit" class="welcome-logout" name="action" value="reset" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out;">Réinitialiser</button>
                </form>


                <% } %>

                <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
                    <table style="width:100%; border-collapse:collapse; color:#fff;">
                        <thead style="background:rgba(255,255,255,.15); text-align: center;">
                        <tr>
                            <th style="padding:10px;">ID</th>
                            <th>Nom du projet</th>
                            <th>Chef de projet</th>
                            <th>Statut</th>
                            <th>Employés affectés</th>
                            <% if (showActions) { %><th>Actions</th> <% } %>
                        </tr>
                        </thead>
                        <tbody>
                        <% if (projects != null) {
                            for (Project p : projects) { %>
                        <tr style="border-bottom:1px solid rgba(255,255,255,.2); text-align: center;">
                            <!-- ID du projet -->
                            <td style="padding:10px;"><%= p.getId() %></td>

                            <!-- Nom du projet avec lien (couleur modifiée en blanc) -->
                            <td style="padding:10px;">
                                <a href="projectDetails.jsp?id=<%= p.getId() %>" style="color: white; text-decoration: none;"
                                   onmouseover="this.style.textDecoration='underline'" onmouseout="this.style.textDecoration='none'">
                                    <%= p.getName() %>
                                </a>
                            </td>
                            <!-- Chef de projet -->
                            <td><%= p.getProjectManager() != null ? p.getProjectManager().getFirstName() + " " + p.getProjectManager().getLastName() : "" %></td>

                            <!-- Statut du projet -->
                            <td><%= p.getStatus().getDisplayName() %></td>

                            <!-- Nombre d'employés affectés -->
                            <td><%= (p.getUsers() != null) ? p.getUsers().size() : 0 %></td>

                            <% if (showActions) { %>
                            <td>
                                <!-- Bouton de modification du projet -->
                                <button type="button" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #fff; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; transition: filter 0.2s ease-in-out;"
                                        onclick='toggleProjectModal({
                                                id:<%= p.getId() %> ,
                                                name:"<%= StringEscapeUtils.escapeEcmaScript(p.getName()) %>",
                                                managerMatricule:"<%= p.getProjectManager() != null ? StringEscapeUtils.escapeEcmaScript(p.getProjectManager().getMatricule()) : "" %>",
                                                status:"<%= p.getStatus() %>",
                                                description:"<%= p.getDescription() != null ? StringEscapeUtils.escapeEcmaScript(p.getDescription()) : "" %>"
                                                })'>Modifier</button>

                                <!-- Formulaire pour supprimer le projet -->
                                <form method="post" action="projects" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                    <input type="hidden" name="id" value="<%= p.getId() %>">
                                    <button type="submit" name="action" value="delete" style="background: #ef4444; color: #fff; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(239, 68, 68, .35); border: none; cursor: pointer; transition: filter 0.2s ease-in-out;">Supprimer</button>
                                </form>
                            </td>
                            <% } %>
                        </tr>
                        <% } } %>
                        </tbody>

                    </table>

                </div>
            </div>
        </main>
    </div>
</div>

<div id="modalAdd" class="modal-container">
    <div class="modal-box">
        <h3 style="color: #fff;">Ajouter un projet</h3>
        <form method="post" action="projects">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">

            <!-- Nom du projet -->
            <label for="nom" style="color: white; font-weight: bold; margin-bottom: 5px;">Nom du projet</label>
            <input id="nom" name="nom" class="input" placeholder="Nom du projet" required
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <!-- Chef de projet -->
            <label for="chefProjet" style="color: white; font-weight: bold; margin-bottom: 5px;">Chef de projet</label>
            <input id="chefProjet" name="chefProjet" list="chefsList" class="input" placeholder="Chef de projet" required oninput="checkManagerValid(this)" data-required="true"
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <!-- Description -->
            <label for="description" style="color: white; font-weight: bold; margin-bottom: 5px;">Description</label>
            <textarea id="description" name="description" class="input" placeholder="Description (optionnel)"
                      style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; height: 80px; margin-bottom: 10px;"></textarea>

            <!-- Statut -->
            <label for="statut" style="color: white; font-weight: bold; margin-bottom: 5px;">Statut</label>
            <select id="statut" name="statut" class="input" required
                    style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">
                <option value="IN_PROGRESS">En cours</option>
                <option value="COMPLETED">Terminé</option>
                <option value="CANCELLED">Annulé</option>
                <option value="PLANNED">Planifié</option>
            </select>

            <!-- Boutons -->
            <button class="btn-save" name="action" value="register">Enregistrer</button>
            <button type="button" class="btn-cancel" onclick="toggleAddModal(false)">Annuler</button>

        </form>
    </div>
</div>


<div id="modalUpdate" class="modal-container">
    <div class="modal-box">

        <h3 style="color: #fff;">Mettre à jour le projet</h3>
        <form id="updateForm" method="post" action="projects">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
            <input type="hidden" name="id">

            <!-- Label et champ "Nom du projet" -->
            <label for="nom" style="color: white; font-weight: bold; margin-bottom: 5px;">Nom du projet</label>
            <input name="nom" class="input" placeholder="Nom du projet" required
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <!-- Label et champ "Chef de projet" -->
            <label for="chefProjet" style="color: white; font-weight: bold; margin-bottom: 5px;">Chef de projet</label>
            <input  name="chefProjet" list="chefsList" class="input" placeholder="Chef de projet" required oninput="checkManagerValid(this)" data-required="true"
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <!-- Label et champ "Description" -->
            <label for="description" style="color: white; font-weight: bold; margin-bottom: 5px;">Description</label>
            <textarea name="description" class="input" placeholder="Description (optionnel)"
                      style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; height: 80px; margin-bottom: 10px;"></textarea>

            <!-- Label et champ "Statut" -->
            <label for="statut" style="color: white; font-weight: bold; margin-bottom: 5px;">Statut</label>
            <select name="statut" class="input" required
                    style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">
                <option value="IN_PROGRESS">En cours</option>
                <option value="COMPLETED">Terminé</option>
                <option value="CANCELLED">Annulé</option>
                <option value="PLANNED">Planifié</option>
            </select>

            <!-- Boutons -->
            <button class="btn-save" name="action" value="update">Mettre à jour</button>
            <button type="button" class="btn-cancel" onclick="toggleProjectModal(null)">Annuler</button>

        </form>
    </div>
</div>




<script src="assets/js/app.js"></script>
</body>
</html>