<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.Department" %>

<%
    String username = "Admin";
    List<Department> departments = (List<Department>) request.getAttribute("departments");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Départements - Gestion RH</title>
  <link href="assets/css/dashboard.css" rel="stylesheet">
</head>
<body>

<div class="bg"></div>
<div class="app-shell">

  <!-- Sidebar -->
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
      <a class="side-link" href="poste.jsp">
        <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 3v18M4 9l4-4 4 4m0 0l4-4 4 4" /></svg>
        <span>Poste</span>
      </a>
      <a class="side-link" href="projects">
        <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 3h18v4H3Zm0 7h18v4H3Zm0 7h18v4H3Z"/></svg>
        <span>Projets</span>
      </a>
      <a class="side-link active" href="departments">
        <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 13h8V3H3Zm10 8h8V3h-8ZM3 21h8v-6H3Z"/></svg>
        <span>Départements</span>
      </a>
      <a class="side-link" href="payslips">
        <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M6 2h9l5 5v15a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm8 1.5V8h4.5ZM8 8h4v2H8Zm0 4h8v2H8Zm0 4h8v2H8Z"/></svg>
        <span>Fiches de paie</span>
      </a>
    </nav>
  </aside>

  <!-- Main content -->
  <div class="main">
    <header class="app-header">
      <div class="brand"><span class="brand-dot"></span><span class="brand-text">Gestion RH</span></div>
      <a href="logout" class="welcome-logout">Se déconnecter</a>
    </header>

    <main class="page">
      <div class="dashboard-container">
        <div class="welcome-card">
          <div class="welcome-left">
            <div>
              <h2 class="welcome-title">Liste des départements</h2>
              <p class="welcome-sub">Connecté en tant que <%= username %></p>
            </div>
          </div>
          <button class="welcome-logout" onclick="openModal()">+ Ajouter</button>
        </div>

        <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
          <table style="width:100%; border-collapse:collapse; color:#fff;">
            <thead style="background:rgba(255,255,255,.15);">
              <tr>
                <th style="padding:10px;">ID</th>
                <th>Nom du département</th>
                <th>Chef</th>
                <th>Employés</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
            <% if (departments != null) {
                 for (Department d : departments) { %>
              <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
                <td style="padding:10px;"><%= d.getId() %></td>
                <td><%= d.getName() %></td>
                <td>
                  <a href="#" class="welcome-logout" style="padding:6px 10px; font-size:.85rem;">Modifier</a>
                  <a href="#" class="welcome-logout" style="padding:6px 10px; font-size:.85rem;">👥 Membres</a>
                  <a href="#" class="welcome-logout" style="padding:6px 10px; font-size:.85rem; background:#ef4444;">Supprimer</a>
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

<script>
function openModal(){ alert("Ajout futur ici"); }
</script>
</body>
</html>
