<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
String username = "Admin";

    int totalEmployees = 120;
    int totalProjects = 15;
    int totalDepartments = 6;
%>

<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8" />
  <title>Tableau de bord - Gestion RH</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="assets/css/dashboard.css" rel="stylesheet" />
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>

<!-- FOND -->
<div class="bg"></div>

<!-- CONTAINER PRINCIPAL -->
<div class="app-shell">

  <!-- SIDEBAR -->
  <aside class="sidebar">
    <nav class="side-nav">
      <a class="side-link active" href="dashboard.jsp">
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
      <a class="side-link" href="payslips.jsp">
        <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M6 2h9l5 5v15a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm8 1.5V8h4.5ZM8 8h4v2H8Zm0 4h8v2H8Zm0 4h8v2H8Z"/></svg>
        <span>Fiches de paie</span>
      </a>
    </nav>
  </aside>

  <!-- CONTENU PRINCIPAL -->
  <div class="main">

    <!-- HEADER -->
    <header class="app-header">
      <div class="brand">
        <span class="brand-dot"></span>
        <span class="brand-text">Gestion RH</span>
      </div>
      <div class="header-actions">
        <button class="avatar-btn" id="avatarBtn" aria-expanded="false">
          <img src="assets/images/avatar-default.png" alt="Profil">
        </button>
        <div class="profile-menu" id="profileMenu" aria-hidden="true">
          <div class="pm-user">
            <img src="assets/images/avatar-default.png" alt="Profil">
            <div>
              <strong class="pm-name"><%= username %></strong>
              <span class="pm-role">Employé</span>
            </div>
          </div>
          <a class="pm-item" href="profile.jsp">Voir le profil</a>
          <a class="pm-item danger" href="logout">Se déconnecter</a>
        </div>
      </div>
    </header>

    <!-- PAGE -->
    <main class="page">
      <div class="dashboard-container">

        <!-- Bienvenue -->
        <div class="welcome-card">
          <div class="welcome-left">
            <span class="wave">👋</span>
            <div>
              <h2 class="welcome-title">Bienvenue, <%= username %></h2>
              <p class="welcome-sub">Voici le résumé de l’activité de votre entreprise</p>
            </div>
          </div>
          <a href="logout" class="welcome-logout">Se déconnecter</a>
        </div>

        <!-- Statistiques -->
        <div class="stats">
          <div class="stat-card"><h5>Employés</h5><h2><%= totalEmployees %></h2></div>
          <div class="stat-card"><h5>Projets</h5><h2><%= totalProjects %></h2></div>
          <div class="stat-card"><h5>Départements</h5><h2><%= totalDepartments %></h2></div>
        </div>

        <!-- CHARTS -->
        <div class="charts-row">
          <div class="chart-card">
            <h6 class="chart-title">Employés par rôle</h6>
            <div class="chart-wrap"><canvas id="chartEmployees"></canvas></div>
          </div>
          <div class="chart-card">
            <h6 class="chart-title">Répartition par département</h6>
            <div class="chart-wrap"><canvas id="chartDepartments"></canvas></div>
          </div>
          <div class="chart-card">
            <h6 class="chart-title">Statut des projets</h6>
            <div class="chart-wrap"><canvas id="chartProjects"></canvas></div>
          </div>
        </div>

        <footer class="footer">© 2025 - Application JEE Gestion RH</footer>
      </div>
    </main>
  </div>
</div>

<!-- JAVASCRIPT -->
<script>
  // === Menu Profil ===
  const avatarBtn = document.getElementById('avatarBtn');
  const profileMenu = document.getElementById('profileMenu');
  function closeMenu(e){
    if(!profileMenu.contains(e.target) && !avatarBtn.contains(e.target)){
      profileMenu.classList.remove('open');
      avatarBtn.setAttribute('aria-expanded','false');
      document.removeEventListener('click', closeMenu);
    }
  }
  avatarBtn.addEventListener('click', () => {
    profileMenu.classList.toggle('open');
    avatarBtn.setAttribute('aria-expanded', profileMenu.classList.contains('open'));
    if (profileMenu.classList.contains('open')) {
      setTimeout(()=>document.addEventListener('click', closeMenu), 0);
    }
  });

  // === Données factices ===
  const employeesByRole = {
    labels: ['Employé', 'Administrateur', 'Chef Département', 'Chef Projet'],
    data: [85, 5, 3, 7]
  };
  const departments = {
    labels: ['RH', 'Finance', 'Informatique', 'Marketing', 'Production', 'R&D'],
    data: [12, 18, 34, 15, 25, 16]
  };
  const projectsStatus = {
    labels: ['En cours', 'Terminés', 'Annulés'],
    data: [9, 4, 2]
  };

  const colorsEmployees   = ['#60a5fa','#f59e0b','#a78bfa','#34d399'];
  const colorsDepartments = ['#22d3ee','#f97316','#84cc16','#e879f9','#f43f5e','#10b981'];
  const colorsProjects    = ['#3b82f6','#10b981','#ef4444'];

  const commonOptions = {
    responsive: true,
    maintainAspectRatio: false,
    layout: { padding: {top:2,right:2,bottom:0,left:2} },
    plugins: {
      legend: { position: 'bottom', labels: { color:'#fff', boxWidth:10, boxHeight:10, font:{ size:10 } } },
    },
    scales: {
      x: { ticks: { color:'#e5e7eb', font:{ size:10 } }, grid: { color:'rgba(255,255,255,.10)' } },
      y: { ticks: { color:'#e5e7eb', font:{ size:10 } }, grid: { color:'rgba(255,255,255,.10)' } }
    }
  };

  // === Chart Employés ===
  new Chart(document.getElementById('chartEmployees'), {
    type: 'doughnut',
    data: {
      labels: employeesByRole.labels,
      datasets: [{
        data: employeesByRole.data,
        backgroundColor: colorsEmployees,
        borderColor: 'rgba(255,255,255,.65)',
        borderWidth: 2
      }]
    },
    options: {
      ...commonOptions,
      plugins: {
        ...commonOptions.plugins,
        title: { display: true, text: 'Répartition des rôles', color: '#fff', font: { size: 12, weight: 'bold' } }
      },
      cutout: '60%'
    }
  });

  // === Chart Départements ===
  new Chart(document.getElementById('chartDepartments'), {
    type: 'bar',
    data: {
      labels: departments.labels,
      datasets: [{
        label: 'Effectif',
        data: departments.data,
        backgroundColor: colorsDepartments,
        borderColor: 'rgba(255,255,255,.35)',
        borderWidth: 1,
        barThickness: 18
      }]
    },
    options: {
      ...commonOptions,
      plugins: {
        ...commonOptions.plugins,
        title: { display: true, text: 'Répartition par département', color: '#fff', font: { size: 12, weight: 'bold' } }
      },
      scales: {
        x: {
          ticks: { color:'#e5e7eb', font:{ size:11 }, autoSkip: false, maxRotation: 0 },
          grid: { color:'rgba(255,255,255,.12)' }
        },
        y: {
          ticks: { color:'#e5e7eb', font:{ size:10 } },
          grid: { color:'rgba(255,255,255,.12)' }
        }
      }
    }
  });

  // === Chart Projets ===
  new Chart(document.getElementById('chartProjects'), {
    type: 'pie',
    data: {
      labels: projectsStatus.labels,
      datasets: [{
        data: projectsStatus.data,
        backgroundColor: colorsProjects,
        borderColor: 'rgba(255,255,255,.65)',
        borderWidth: 2
      }]
    },
    options: {
      ...commonOptions,
      plugins: {
        ...commonOptions.plugins,
        title: { display: true, text: 'Statut des projets', color: '#fff', font: { size: 12, weight: 'bold' } }
      }
    }
  });
</script>

</body>
</html>
