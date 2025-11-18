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
    <a class="side-link" href="employees.jsp">
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
                    <input name="name"
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
                    <input name="manager"
                           list="chefsList"
                           placeholder="Chef de projet"
                           value="<%= request.getAttribute("filter_manager") %>"
                           oninput="checkManagerValid(this)" data-required="false"
                           style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 200px;">

                    <!-- Statut -->
                    <select name="status" style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 150px;">
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
                                <td style="padding:10px;"><%= p.getId() %></td>
                                <td><%= p.getName() %></td>
                                <td><%= p.getProjectManager() != null ? p.getProjectManager().getFirstName() + " " + p.getProjectManager().getLastName() : "" %></td>
                                <td><%= p.getStatus().getDisplayName() %></td>
                                <td><%= (p.getUsers() != null) ? p.getUsers().size() : 0 %></td>
                                <% if (showActions) { %>
                                <td>
                                    <button type="button" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #fff; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; transition: filter 0.2s ease-in-out;"
                                            onclick='toggleProjectModal({
                                                    id:<%= p.getId() %>,
                                                    name:"<%= StringEscapeUtils.escapeEcmaScript(p.getName()) %>",
                                                    managerMatricule:"<%= p.getProjectManager() != null ? StringEscapeUtils.escapeEcmaScript(p.getProjectManager().getMatricule()) : "" %>",
                                                    status:"<%= p.getStatus() %>",
                                                    description:"<%= p.getDescription() != null ? StringEscapeUtils.escapeEcmaScript(p.getDescription()) : "" %>"
                                                    })'>Modifier</button>

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

<div id="modalAdd" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.6); z-index:999; justify-content:center; align-items:center;">
    <div style="background:rgba(255,255,255,.15); padding:20px; border-radius:14px; backdrop-filter:blur(10px); width:400px; box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);">
        <h3 style="color: #fff;">Ajouter un projet</h3>
        <form method="post" action="projects">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">

            <!-- Nom du projet -->
            <label for="nom" style="color: white; font-weight: bold; margin-bottom: 5px;">Nom du projet</label>
            <input id="nom" name="nom" class="input" placeholder="Nom du projet" required
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <!-- Chef de projet -->
            <label for="chefProjet" style="color: white; font-weight: bold; margin-bottom: 5px;">Chef de projet</label>
            <input id="chefProjet" name="chefProjet" class="input" placeholder="Chef de projet" required
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
            <button class="welcome-logout"
                    style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out; width: 100%; margin-top: 10px;"
                    name="action" value="register">Enregistrer</button>
            <button type="button" class="welcome-logout"
                    style="background: #ef4444; color: #fff; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(239, 68, 68, .35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out; width: 100%; margin-top: 10px;"
                    onclick="toggleAddModal(false)">Annuler</button>
        </form>
    </div>
</div>


<div id="modalUpdate" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.6); z-index:999; justify-content:center; align-items:center;">
    <div style="background:rgba(255,255,255,.15); padding:20px; border-radius:14px; backdrop-filter:blur(10px); width:400px; box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);">
        <h3 style="color: #fff;">Mettre à jour le projet</h3>
        <form id="updateForm" method="post" action="projects">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
            <input type="hidden" name="id">

            <!-- Label et champ "Nom du projet" -->
            <label for="nom" style="color: white; font-weight: bold; margin-bottom: 5px;">Nom du projet</label>
            <input id="nom" name="nom" class="input" placeholder="Nom du projet" required
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <!-- Label et champ "Chef de projet" -->
            <label for="chefProjet" style="color: white; font-weight: bold; margin-bottom: 5px;">Chef de projet</label>
            <input id="chefProjet" name="chefProjet" class="input" placeholder="Chef de projet" required
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">

            <!-- Label et champ "Description" -->
            <label for="description" style="color: white; font-weight: bold; margin-bottom: 5px;">Description</label>
            <textarea id="description" name="description" class="input" placeholder="Description (optionnel)"
                      style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; height: 80px; margin-bottom: 10px;"></textarea>

            <!-- Label et champ "Statut" -->
            <label for="statut" style="color: white; font-weight: bold; margin-bottom: 5px;">Statut</label>
            <select id="statut" name="statut" class="input" required
                    style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); width: 100%; margin-bottom: 10px;">
                <option value="IN_PROGRESS">En cours</option>
                <option value="COMPLETED">Terminé</option>
                <option value="CANCELLED">Annulé</option>
                <option value="PLANNED">Planifié</option>
            </select>

            <!-- Boutons -->
            <button class="welcome-logout"
                    style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out; width: 100%; margin-top: 10px;"
                    name="action" value="update">Mettre à jour</button>
            <button type="button" class="welcome-logout"
                    style="background: #ef4444; color: #fff; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(239, 68, 68, .35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out; width: 100%; margin-top: 10px;"
                    onclick="toggleProjectModal(null)">Annuler</button>
        </form>
    </div>
</div>




<script src="assets/js/app.js"></script>
</body>
</html>