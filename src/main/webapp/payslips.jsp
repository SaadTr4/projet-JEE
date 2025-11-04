<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.models.Payslip" %>

<%
    String username = "Admin";
    List<Payslip> payslips = (List<Payslip>) request.getAttribute("payslips");
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
    <a class="side-link" href="projects.jsp">
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
                    <button class="welcome-logout" onclick="openModal()">+ Générer</button>
                </div>

                <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
                    <table style="width:100%; border-collapse:collapse; color:#fff;">
                        <thead style="background:rgba(255,255,255,.15);">
                            <tr>
                                <th>ID</th>
                                <th>Employé</th>
                                <th>Salaire (€)</th>
                                <th>Prime (€)</th>
                                <th>Déduction (€)</th>
                                <th>Total (€)</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (payslips != null) {
                            for (Payslip p : payslips) { %>
                            <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
                                <td><%= p.getId() %></td>
                                <td><%= p.getEmployeNom() %></td>
                                <td><%= p.getSalaireBase() %></td>
                                <td><%= p.getPrime() %></td>
                                <td><%= p.getDeduction() %></td>
                                <td><%= p.getTotal() %></td>
                                <td>
                                    <a href="#" class="welcome-logout" style="padding:6px 10px; font-size:.85rem;">Exporter PDF</a>
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

<!-- Modal -->
<div id="modalAdd" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.6); z-index:999; justify-content:center; align-items:center;">
    <div style="background:rgba(255,255,255,.1); padding:20px; border-radius:14px; backdrop-filter:blur(10px); width:400px;">
        <h3>Générer une fiche de paie</h3>
        <form method="post" action="GeneratePayslipServlet">
            <input name="employeNom" class="input" placeholder="Nom de l'employé" required>
            <input name="salaire" type="number" class="input" placeholder="Salaire de base (€)" required>
            <input name="prime" type="number" class="input" placeholder="Prime (€)" value="0">
            <input name="deduction" type="number" class="input" placeholder="Déduction (€)" value="0">
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
