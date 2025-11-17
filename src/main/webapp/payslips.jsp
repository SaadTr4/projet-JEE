<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.Payslip" %>
<%@ page import="fr.projetjee.model.User" %>
<%@ page import="fr.projetjee.enums.Role" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    String username = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Invité";

    List<Payslip> payslips = (List<Payslip>) request.getAttribute("payslips");
    List<User> employees = (List<User>) request.getAttribute("employees");

    String[] months = {"Janvier","Février","Mars","Avril","Mai","Juin","Juillet","Août","Septembre","Octobre","Novembre","Décembre"};

    boolean canAccess = currentUser != null &&
            (currentUser.getRole() == Role.ADMINISTRATEUR ||
                    (currentUser.getRole() == Role.EMPLOYE && currentUser.getDepartment() != null && "RH".equalsIgnoreCase(currentUser.getDepartment().getCode())));
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Fiches de paie - Gestion RH</title>
    <link href="assets/css/dashboard.css" rel="stylesheet">
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
    <a class="side-link" href="employees.jsp">
      <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm-7 9a7 7 0 0 1 14 0Z"/></svg>
      <span>Employés</span>
    </a>
    <a class="side-link" href="projects">
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
                        <span class="wave">🧾</span>
                        <div>
                            <h2 class="welcome-title">Fiches de paie</h2>
                            <p class="welcome-sub">Connecté en tant que <%= username %></p>
                        </div>
                    </div>
                    <% if (canAccess) { %>
                    <button class="welcome-logout" onclick="toggleAddModal(true)">+ Générer</button>
                    <% } %>
                </div>

                <% if (canAccess) { %>
                <!-- FILTRES -->
                <div class="filter-card">
                    <form method="post" action="payslips" style="display:flex; gap:16px; align-items:center;">
                        <input type="hidden" name="action" value="filter">
                        <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">

                        <!-- Employé -->
                        <div>
                            <input list="employeesList" name="user" class="input"  value="<%= request.getAttribute("filter_user") %>" placeholder="ex: EMP123" oninput="checkEmployeeValid(this)" data-required="false">
                            <datalist id="employeesList">
                                <% if (employees != null) {
                                    for (User u : employees) { %>
                                <option value="<%= u.getMatricule() %>"><%= u.getFullName() %></option>
                                <% }} %>
                            </datalist>
                        </div>

                        <!-- Année -->
                        <div>
                            <input name="year" class="input" type="number" value="<%= request.getAttribute("filter_year") %>" placeholder="ex : 2024">
                        </div>

                        <!-- Mois -->
                        <div>
                            <select name="month" class="input">
                                <option value="">Tous</option>
                                <% Integer filterMonth = (Integer) request.getAttribute("filter_month");
                                    for (int m = 1; m <= 12; m++) { %>
                                <option value="<%= m %>" <%= (filterMonth != null && filterMonth == m) ? "selected" : "" %>><%= months[m-1] %></option>
                                <% } %>
                            </select>
                        </div>

                        <button class="welcome-logout">Filtrer</button>
                    </form>

                    <form method="post" action="payslips">
                        <input type="hidden" name="action" value="reset">
                        <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                        <button class="welcome-logout" style="margin-top:10px; background:#ef4444;">Réinitialiser</button>
                    </form>

                </div>
                <% } %>

                <% if (request.getAttribute("error") != null) { %>
                <div style="color:#ef4444; font-weight:bold; margin:16px 0; text-align:center;">
                    <%= request.getAttribute("error") %>
                </div>
                <% } %>

                <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
                    <table style="width:100%; border-collapse:collapse; color:#fff;">
                        <thead style="background:rgba(255,255,255,.15);">
                            <tr>
                                <th>ID</th>
                                <% if (canAccess) { %> <th>Employé</th> <% } %>
                                <th>Salaire</th>
                                <th>Année</th>
                                <th>Mois</th>
                                <th>Total (€)</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (payslips != null) {
                            for (Payslip p : payslips) { %>
                            <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
                                <td><%= p.getId() %></td>
                                <% if (canAccess) { %> <td><%= p.getUser().getFullName() %></td> <% } %>
                                <td><%= p.getBaseSalary() %></td>
                                <td><%= p.getYear() %></td>
                                <td><%= p.getMonth()%></td>
                                <td><%= p.getNetPay() %></td>

                                <td>
                                    <% if (canAccess) { %>
                                    <button class="welcome-logout" style="background:#3b82f6; padding:4px 8px;"
                                            onclick='togglePayslipModal({
                                                    id: "<%= p.getId() %>",
                                                    baseSalary: "<%= p.getBaseSalary() %>",
                                                    bonuses: "<%= p.getBonuses() %>",
                                                    deductions: "<%= p.getDeductions() %>",
                                                    userFullName : "<%= p.getUser().getFullName() %>",
                                                    userMatricule : "<%= p.getUser().getMatricule() %>",
                                                    year: <%= p.getYear() %>,
                                                    month: <%= p.getMonth() %>
                                                    })'>Modifier
                                    </button>
                                    <% } %>

                                    <form method="post" action="payslips" style="display:inline;">
                                        <input type="hidden" name="action" value="export">
                                        <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                        <input type="hidden" name="id" value="<%= p.getId() %>">
                                        <button class="welcome-logout" style="padding:4px 8px;">PDF</button>
                                    </form>

                                    <% if (canAccess) { %>
                                    <form method="post" action="payslips" style="display:inline;">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                                        <input type="hidden" name="id" value="<%= p.getId() %>">
                                        <button class="welcome-logout" style="background:#ef4444; padding:4px 8px;">Supprimer</button>
                                    </form>
                                    <% } %>
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

<!-- Modal -->
<div id="modalAdd" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.6); z-index:999; justify-content:center; align-items:center;">
    <div style="background:rgba(255,255,255,.1); padding:20px; border-radius:14px; backdrop-filter:blur(10px); width:400px;">
        <h3>Générer une fiche de paie</h3>
        <form method="post" action="payslips">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
            <label>Matricule employé :</label>
            <input list="employeesList" name="user" class="input"  placeholder="ex: E123" oninput="checkEmployeeValid(this)" data-required="true" required>

            <label>Année :</label>
            <input type="number" name="year" class="input" required>

            <label>Mois :</label>
            <select name="month" class="input" required>
                <% for (int i = 0; i < months.length; i++) { %>
                <option value="<%= i+1 %>"><%= months[i] %></option>
                <% } %>
            </select>

            <label>Salaire de base :</label>
            <input type="number" name="baseSalary" class="input" step="0.01" placeholder="Salaire de base (€)" required>

            <label>Prime :</label>
            <input type="number" name="bonuses" class="input" step="0.01" placeholder="Prime (€)" value="0" required>

            <label>Déduction :</label>
            <input type="number" name="deductions" class="input" step="0.01" placeholder="Déduction (€)" value="0" required>

            <button class="welcome-logout" style="margin-top:10px;" name="action"  value="create">Enregistrer</button>
            <button type="button" class="welcome-logout" style="background:#ef4444; margin-top:10px;" onclick="closeModal()">Annuler</button>
        </form>
    </div>
</div>

<!-- Modal Modification Fiche de Paie -->
<div id="modalUpdate" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.6); z-index:999; justify-content:center; align-items:center;">
    <div style="background:rgba(255,255,255,.1); padding:20px; border-radius:14px; backdrop-filter:blur(10px); width:400px;">
        <h3>Modifier la fiche de paie</h3>
        <form method="post" action="payslips" id="updateForm">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" id="id">

            <!-- Nom et prénom affichés mais non modifiables -->
            <label>Employé :</label>
            <input type="text" id="employeeName" class="input" disabled>

            <!-- Matricule affiché mais non modifiable -->
            <label>Matricule :</label>
            <input type="text" id="employeeMatricule" class="input" disabled>

            <!-- Année et mois affichés mais non modifiables -->
            <label>Année :</label>
            <input type="text" id="yearDisplay" class="input" disabled>

            <label>Mois :</label>
            <input type="text" id="monthDisplay" class="input" disabled>

            <label>Salaire de base :</label>
            <input type="number" name="baseSalary" id="baseSalary" class="input" step="0.01" required>

            <label>Prime :</label>
            <input type="number" name="bonuses" id="bonuses" class="input" step="0.01" value="0" required>

            <label>Déduction :</label>
            <input type="number" name="deductions" id="deductions" class="input" step="0.01" value="0" required>

            <button class="welcome-logout" style="margin-top:10px;">Enregistrer</button>
            <button type="button" class="welcome-logout" style="background:#ef4444; margin-top:10px;" onclick="togglePayslipModal(null)">Annuler</button>
        </form>
    </div>
</div>


<script src="assets/js/app.js"></script>
</body>
</html>
