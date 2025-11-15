<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.Project" %>
<%@ page import="fr.projetjee.model.User" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>


<%
    String username = "Admin";
    List<Project> projects = (List<Project>) request.getAttribute("projects");
    List<User> chefs = (List<User>) request.getAttribute("chefs");
    List<Project> allProjects = (List<Project>) request.getAttribute("allProjects");

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
                        <span class="wave">👋</span>
                        <div>
                            <h2 class="welcome-title">Liste des projets</h2>
                            <p class="welcome-sub">Connecté en tant que <%= username %></p>
                        </div>
                    </div>
                    <button class="welcome-logout" onclick="toggleAddModal(true)">+ Ajouter</button>
                </div>

                <form method="post" action="projects" style="margin-bottom:16px; display:flex; gap:10px; flex-wrap:wrap; align-items:center;">
                    <!-- Nom du projet -->
                    <input name="name"
                           list="projectsList"
                           placeholder="Nom du projet"
                           value="<%= request.getAttribute("filter_name") %>"
                           oninput="checkProjectValid(this)">

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
                           oninput="checkManagerValid(this)" data-required="false">

                    <!-- Statut -->
                    <select name="status">
                        <option value="">Tous</option>
                        <option value="IN_PROGRESS" <%= "IN_PROGRESS".equals(request.getAttribute("filter_status")) ? "selected" : "" %>>En cours</option>
                        <option value="COMPLETED" <%= "COMPLETED".equals(request.getAttribute("filter_status")) ? "selected" : "" %>>Terminé</option>
                        <option value="CANCELLED" <%= "CANCELLED".equals(request.getAttribute("filter_status")) ? "selected" : "" %>>Annulé</option>
                        <option value="PLANNED" <%= "PLANNED".equals(request.getAttribute("filter_status")) ? "selected" : "" %>>Planifié</option>
                    </select>

                    <button type="submit" class="welcome-logout" name="action" value="filter">Filtrer</button>
                    <button type="submit" class="welcome-logout" name="action" value="reset">Réinitialiser</button>
                </form>

                <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
                    <table style="width:100%; border-collapse:collapse; color:#fff;">
                        <thead style="background:rgba(255,255,255,.15);">
                            <tr>
                                <th style="padding:10px;">ID</th>
                                <th>Nom du projet</th>
                                <th>Chef de projet</th>
                                <th>Statut</th>
                                <th>Employés affectés</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (projects != null) {
                               for (Project p : projects) { %>
                            <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
                                <td style="padding:10px;"><%= p.getId() %></td>
                                <td><%= p.getName() %></td>
                                <td><%= p.getProjectManager() != null ? p.getProjectManager().getFirstName() + " " + p.getProjectManager().getLastName() : "" %></td>
                                <td><%= p.getStatus().getDisplayName() %></td>
                                <td><%= (p.getUsers() != null) ? p.getUsers().size() : 0 %></td>
                                <td><button type="button" class="welcome-logout"
                                            onclick='toggleProjectModal({
                                                    id:<%= p.getId() %>,
                                                    name:"<%= StringEscapeUtils.escapeEcmaScript(p.getName()) %>",
                                                    managerMatricule:"<%= p.getProjectManager() != null ? StringEscapeUtils.escapeEcmaScript(p.getProjectManager().getMatricule()) : "" %>",
                                                    status:"<%= p.getStatus() %>",
                                                    description:"<%= p.getDescription() != null ? StringEscapeUtils.escapeEcmaScript(p.getDescription()) : "" %>"
                                                    })'>
                                    Modifier
                                </button>

                                    <form method="post" action="projects" style="display:inline;">
                                        <input type="hidden" name="id" value="<%= p.getId() %>">
                                        <button type="submit" name="action" value="delete"
                                                class="welcome-logout"
                                                style="padding:6px 10px; font-size:.85rem; background:#ef4444;">Supprimer</button>
                                    </form>
                                </td>
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
    <div style="background:rgba(255,255,255,.1); padding:20px; border-radius:14px; backdrop-filter:blur(10px); width:400px;">
        <h3>Ajouter un projet</h3>
        <form method="post" action="projects">
            <input name="nom" class="input" placeholder="Nom du projet" required>
            <input name="chefProjet" list="chefsList" placeholder="Chef de projet" required oninput="checkManagerValid(this)" data-required="true">
            <select name="statut" class="input" required>
                <option value="IN_PROGRESS">En cours</option>
                <option value="COMPLETED">Terminé</option>
                <option value="CANCELLED">Annulé</option>
                <option value="PLANNED">Planifié</option>
            </select>
            <button class="welcome-logout" style="margin-top:10px;"  name="action"  value="register">Enregistrer</button>
            <button type="button" class="welcome-logout" style="background:#ef4444; margin-top:10px;" onclick="toggleAddModal(false)">Annuler</button>
        </form>
    </div>
</div>

<div id="modalUpdate" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.6); z-index:999; justify-content:center; align-items:center;">
    <div style="background:rgba(255,255,255,.1); padding:20px; border-radius:14px; backdrop-filter:blur(10px); width:400px;">
        <h3>Mettre à jour le projet</h3>
        <form id="updateForm" method="post" action="projects">
            <input type="hidden" name="id">
            <input name="nom" class="input" placeholder="Nom du projet" required>
            <input name="chefProjet" list="chefsList" placeholder="Chef de projet" required oninput="checkManagerValid(this)" data-required="true">
            <select name="statut" class="input" required>
                <option value="IN_PROGRESS">En cours</option>
                <option value="COMPLETED">Terminé</option>
                <option value="CANCELLED">Annulé</option>
                <option value="PLANNED">Planifié</option>
            </select>
            <textarea name="description" class="input" placeholder="Description (optionnel)" style="height:80px;"></textarea>
            <button class="welcome-logout" style="margin-top:10px;"  name="action"  value="update">Mettre à jour</button>
            <button type="button" class="welcome-logout" style="background:#ef4444; margin-top:10px;" onclick="toggleProjectModal(null)">Annuler</button>
        </form>
    </div>
</div>


<script src="assets/js/app.js"></script>
</body>
</html>