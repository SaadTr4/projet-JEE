<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.Department" %>
<%@ page import="fr.projetjee.model.User" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="fr.projetjee.enums.Role" %>

<%
  List<Department> departments = (List<Department>) request.getAttribute("departments");
  List<User> employees = (List<User>) request.getAttribute("employees");

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
    }
  }

  String filterName = (String) request.getAttribute("filter_name");
  String filterCode = (String) request.getAttribute("filter_code");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Départements - Gestion RH</title>
  <link href="assets/css/dashboard.css" rel="stylesheet">
  <style>
    /* Styles modale identiques à projects.jsp */
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

    .filter-input {
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
  </style>
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
      <a class="side-link" href="position.jsp">
        <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 3v18M4 9l4-4 4 4m0 0l4-4 4 4" /></svg>
        <span>Postes</span>
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
          <% if (showAddButton) { %>
          <button class="welcome-logout" onclick="toggleAddModal(true)" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem;">+ Ajouter</button>
          <% } %>
        </div>

        <!-- Affichage des erreurs -->
        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
        <div style="background: rgba(239, 68, 68, 0.2); border: 1px solid rgba(239, 68, 68, 0.5); color: white; padding: 12px; border-radius: 10px; margin-bottom: 16px; backdrop-filter: blur(8px);">
          <%= error %>
        </div>
        <% } %>

        <!-- Filtres -->
        <% if (showFilter) { %>
        <form method="post" action="departments" style="margin-bottom:16px; display:flex; gap:10px; flex-wrap:wrap; align-items:center;">
          <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">

          <input name="name" class="filter-input" placeholder="Nom du département"
                 value="<%= filterName != null ? filterName : "" %>"
                 style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 200px;">

          <input name="code" class="filter-input" placeholder="Code du département"
                 value="<%= filterCode != null ? filterCode : "" %>"
                 style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 200px;">

          <button type="submit" class="welcome-logout" name="action" value="filter" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer;">Filtrer</button>
          <button type="submit" class="welcome-logout" name="action" value="reset" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer;">Réinitialiser</button>
        </form>
        <% } %>

        <!-- Tableau des départements -->
        <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
          <table style="width:100%; border-collapse:collapse; color:#fff;">
            <thead style="background:rgba(255,255,255,.15);">
            <tr>
              <th style="padding:10px;">ID</th>
              <th>Nom</th>
              <th>Code</th>
              <th>Description</th>
              <th>Nombre d'employés</th>
              <% if (showActions) { %><th>Actions</th><% } %>
            </tr>
            </thead>
            <tbody>
            <% if (departments != null && !departments.isEmpty()) {
              for (Department d : departments) {
                long userCount = 0;
                try {
                  // Cette méthode doit être implémentée dans DepartmentDAO
                  userCount = d.getUsers() != null ? d.getUsers().size() : 0;
                } catch (Exception e) {
                  userCount = 0;
                }
            %>
            <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
              <td style="padding:10px;"><%= d.getId() %></td>
              <td>
                <a href="departmentdetails?id=<%= d.getId() %>"
                   style="color: white; text-decoration: none; font-weight: 500;"
                   onmouseover="this.style.textDecoration='underline'"
                   onmouseout="this.style.textDecoration='none'">
                  <%= d.getName() %>
                </a>
              </td>
              <td><%= d.getCode() %></td>
              <td><%= d.getDescription() != null ? d.getDescription() : "" %></td>
              <td style="text-align: center;"><%= userCount %></td>
              <% if (showActions) { %>
              <td class="action-buttons">
                <button type="button" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #fff; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer;"
                        onclick='toggleUpdateModal({
                                id:<%= d.getId() %>,
                                name:"<%= StringEscapeUtils.escapeEcmaScript(d.getName()) %>",
                                code:"<%= StringEscapeUtils.escapeEcmaScript(d.getCode()) %>",
                                description:"<%= d.getDescription() != null ? StringEscapeUtils.escapeEcmaScript(d.getDescription()) : "" %>"
                                })'>Modifier</button>

                <form method="post" action="departments" style="display:inline;">
                  <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                  <input type="hidden" name="id" value="<%= d.getId() %>">
                  <button type="submit" name="action" value="delete" style="background: #ef4444; color: #fff; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(239, 68, 68, .35); border: none; cursor: pointer;"
                          onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce département ?')">Supprimer</button>
                </form>

                <button type="button" style="background: linear-gradient(135deg, #10b981, #059669); color: #fff; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(16, 185, 129, .35); border: none; cursor: pointer;"
                        onclick='toggleAssignModal(<%= d.getId() %>, "<%= StringEscapeUtils.escapeEcmaScript(d.getName()) %>")'>Assigner</button>
              </td>
              <% } %>
            </tr>
            <% } } else { %>
            <tr>
              <td colspan="<%= showActions ? 6 : 5 %>" style="text-align: center; padding: 20px; color: rgba(255,255,255,0.7);">
                Aucun département trouvé
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

<!-- Modale Ajout -->
<div id="modalAdd" class="modal-container">
  <div class="modal-box">
    <h3>Ajouter un département</h3>
    <form method="post" action="departments">
      <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">

      <input name="name" placeholder="Nom du département" required
             style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%; margin-bottom: 10px;">

      <input name="code" placeholder="Code du département (ex: RH, IT)" required
             style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%; margin-bottom: 10px;">

      <textarea name="description" placeholder="Description (optionnel)"
                style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%; height: 80px; margin-bottom: 10px;"></textarea>

      <button class="btn-save" name="action" value="register">Enregistrer</button>
      <button type="button" class="btn-cancel" onclick="toggleAddModal(false)">Annuler</button>
    </form>
  </div>
</div>

<!-- Modale Modification -->
<div id="modalUpdate" class="modal-container">
  <div class="modal-box">
    <h3>Modifier le département</h3>
    <form method="post" action="departments">
      <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
      <input type="hidden" name="id" id="updateId">

      <input name="name" id="updateName" placeholder="Nom du département" required
             style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%; margin-bottom: 10px;">

      <input name="code" id="updateCode" placeholder="Code du département" required
             style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%; margin-bottom: 10px;">

      <textarea name="description" id="updateDescription" placeholder="Description"
                style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%; height: 80px; margin-bottom: 10px;"></textarea>

      <button class="btn-save" name="action" value="update">Mettre à jour</button>
      <button type="button" class="btn-cancel" onclick="toggleUpdateModal(null)">Annuler</button>
    </form>
  </div>
</div>

<!-- Modale Assignation -->
<div id="modalAssign" class="modal-container">
  <div class="modal-box">
    <h3 id="assignTitle">Assigner un employé</h3>
    <form method="post" action="departments">
      <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
      <input type="hidden" name="departmentId" id="assignDepartmentId">

      <select name="registrationNumber" required
              style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%; margin-bottom: 10px;">
        <option value="">Sélectionner un employé</option>
        <% if (employees != null) { for (User emp : employees) { %>
        <option value="<%= emp.getMatricule() %>">
          <%= emp.getMatricule() %> - <%= emp.getFirstName() %> <%= emp.getLastName() %>
        </option>
        <% } } %>
      </select>

      <button class="btn-save" name="action" value="assign">Assigner</button>
      <button type="button" class="btn-cancel" onclick="toggleAssignModal(null, null)">Annuler</button>
    </form>
  </div>
</div>

<script>
  // Fonctions pour gérer les modales
  function toggleAddModal(show) {
    document.getElementById('modalAdd').style.display = show ? 'flex' : 'none';
  }

  function toggleUpdateModal(department) {
    const modal = document.getElementById('modalUpdate');
    if (department) {
      document.getElementById('updateId').value = department.id;
      document.getElementById('updateName').value = department.name;
      document.getElementById('updateCode').value = department.code;
      document.getElementById('updateDescription').value = department.description || '';
      modal.style.display = 'flex';
    } else {
      modal.style.display = 'none';
    }
  }

  function toggleAssignModal(departmentId, departmentName) {
    const modal = document.getElementById('modalAssign');
    if (departmentId) {
      document.getElementById('assignDepartmentId').value = departmentId;
      document.getElementById('assignTitle').textContent = 'Assigner un employé - ' + departmentName;
      modal.style.display = 'flex';
    } else {
      modal.style.display = 'none';
    }
  }

  // Fermer les modales en cliquant à l'extérieur
  document.addEventListener('click', function(event) {
    const modals = ['modalAdd', 'modalUpdate', 'modalAssign'];
    modals.forEach(modalId => {
      const modal = document.getElementById(modalId);
      if (event.target === modal) {
        modal.style.display = 'none';
      }
    });
  });
</script>
</body>
</html>