<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.User" %>

<%
    // Pas de vérification de session (test)
    String username = "Admin";

    List<User> employees = new ArrayList<>();
    //    public User(String matricule, String lastName, String firstName, String email) {
    employees.add(new User("E1","Dupont","Jean","jean.dupont@entreprise.com"));
    employees.add(new User("E5","jk","Jeahn","jean.dupgont@entreprise.com"));
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Employés - Gestion RH</title>
    <link href="assets/css/dashboard.css" rel="stylesheet">
</head>
<body>
<div class="bg"></div>

<div class="app-shell">

    <!-- ✅ SIDEBAR (identique au dashboard) -->
    <aside class="sidebar">
      <nav class="side-nav">
        <a class="side-link" href="dashboard.jsp">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="currentColor" d="M11 3 2 9v12h7v-7h6v7h7V9z"/>
          </svg>
          <span>Tableau de bord</span>
        </a>
        <a class="side-link active" href="employees.jsp">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="currentColor" d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm-7 9a7 7 0 0 1 14 0Z"/>
          </svg>
          <span>Employés</span>
        </a>
        <a class="side-link" href="projects">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="currentColor" d="M3 3h18v4H3Zm0 7h18v4H3Zm0 7h18v4H3Z"/>
          </svg>
          <span>Projets</span>
        </a>
        <a class="side-link" href="departments.jsp">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="currentColor" d="M3 13h8V3H3Zm10 8h8V3h-8ZM3 21h8v-6H3Z"/>
          </svg>
          <span>Départements</span>
        </a>
        <a class="side-link" href="payslips">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="currentColor" d="M6 2h9l5 5v15a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm8 1.5V8h4.5ZM8 8h4v2H8Zm0 4h8v2H8Zm0 4h8v2H8Z"/>
          </svg>
          <span>Fiches de paie</span>
        </a>
      </nav>
    </aside>

    <!-- CONTENU PRINCIPAL -->
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
                            <h2 class="welcome-title">Liste des employés</h2>
                            <p class="welcome-sub">Connecté en tant que <%= username %></p>
                        </div>
                    </div>
                    <button class="welcome-logout" onclick="openModal()">+ Ajouter</button>
                </div>

                <!-- TABLEAU -->
                <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
                    <table style="width:100%; border-collapse:collapse; color:#fff;">
                        <thead style="background:rgba(255,255,255,.15);">
                            <tr>
                                <th style="padding:10px;">ID</th>
                                <th>Nom</th>
                                <th>Prénom</th>
                                <th>Email</th>
                                <th>Rôle</th>
                                <th>Département</th>
                                <th>Projet</th>
                                <th>Salaire (€)</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% for (User e : employees) { %>
                            <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
                                <td style="padding:10px;"><%= e.getId() %></td>
                                <td><%= e.getLastName() %></td>
                                <td><%= e.getFirstName() %></td>
                                <td><%= e.getEmail() %></td>
                                <td><%= e.getRole() %></td>
                                <td><%= e.getProjects() %></td>
                                <td>
                                    <a href="EditEmployeeServlet?id=<%= e.getId() %>" class="welcome-logout">Modifier</a>
                                    <a href="DeleteEmployeeServlet?id=<%= e.getId() %>" class="welcome-logout" style="background:#ef4444;">Supprimer</a>
                                    
                                    
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

<!-- MODAL -->
<div id="modalAdd" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.6); z-index:999; justify-content:center; align-items:center;">
    <div style="background:rgba(255,255,255,.1); padding:20px; border-radius:14px; backdrop-filter:blur(10px); width:400px;">
        <h3>Ajouter un employé</h3>
        <form method="post" action="AddEmployeeServlet">
            <input name="nom" class="input" placeholder="Nom" required>
            <input name="prenom" class="input" placeholder="Prénom" required>
            <input name="email" class="input" placeholder="Email" required>
            <input name="role" class="input" placeholder="Rôle" required>
            <input name="departement" class="input" placeholder="Département" required>
            <input name="projet" class="input" placeholder="Projet">
            <input name="salaire" type="number" class="input" placeholder="Salaire (€)" required>
            <button class="welcome-logout" style="margin-top:10px;">Enregistrer</button>
            <button type="button" class="welcome-logout" style="background:#ef4444; margin-top:10px;" onclick="closeModal()">Annuler</button>
        </form>
    </div>
</div>

<script>
function openModal(){ document.getElementById('modalAdd').style.display='flex'; }
function closeModal(){ document.getElementById('modalAdd').style.display='none'; }
</script>
</body>
</html>

