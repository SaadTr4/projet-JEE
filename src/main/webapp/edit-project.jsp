<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.projetjee.models.Project" %>

<%
    Project p = (Project) request.getAttribute("project");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Modifier un projet</title>
    <link href="assets/css/dashboard.css" rel="stylesheet">
</head>
<body>
<div class="bg"></div>

<div class="app-shell">
    <div class="main">
        <header class="app-header">
            <div class="brand">
                <span class="brand-dot"></span>
                <span class="brand-text">Gestion RH</span>
            </div>
        </header>

        <main class="page">
            <div class="dashboard-container">
                <div class="welcome-card">
                    <h2>Modifier le projet</h2>
                </div>

                <div class="chart-card" style="padding:20px;">
                    <form action="EditProjectServlet" method="post">
                        <input type="hidden" name="id" value="<%= p.getId() %>">

                        <input name="nom" value="<%= p.getNom() %>" placeholder="Nom du projet" required>
                        <input name="chefProjet" value="<%= p.getChefProjet() %>" placeholder="Chef de projet" required>
                        <select name="statut" required>
                            <option value="En cours" <%= "En cours".equals(p.getStatut()) ? "selected" : "" %>>En cours</option>
                            <option value="Terminé" <%= "Terminé".equals(p.getStatut()) ? "selected" : "" %>>Terminé</option>
                            <option value="Annulé" <%= "Annulé".equals(p.getStatut()) ? "selected" : "" %>>Annulé</option>
                        </select>

                        <button class="welcome-logout" style="margin-top:10px;">Enregistrer</button>
                        <a href="projects" class="welcome-logout" style="background:#ef4444;">Annuler</a>
                    </form>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
