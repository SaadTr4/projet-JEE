<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.User" %>
<%@ page import="fr.projetjee.enums.Role" %>

<%
    // Valeurs statiques pour tester
    List<String> posts = Arrays.asList("Développeur Backend", "Responsable RH", "Chef de projet");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Postes - Gestion RH</title>
    <link href="assets/css/dashboard.css" rel="stylesheet">
    <style>
        .modal-container {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(0,0,0,0.45);
            backdrop-filter: blur(6px);
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

        .filter-input, .filter-select {
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
        .btn-action {
            font-size: 0.85rem;
            padding: 6px 10px;
            margin-right: 10px;  /* Ajoute de l'espace entre les deux boutons */
            border-radius: 8px;  /* Réduit les coins pour un effet plus léger */
            font-weight: 600;
            box-shadow: 0 8px 22px rgba(59,130,246,.35);
            transition: filter 0.2s ease-in-out;
        }

        /* Bouton Modifier */
        .btn-modify {
            background: linear-gradient(135deg, #3b82f6, #06b6d4);
            color: white;
            border: none;
            cursor: pointer;
        }

        /* Bouton Supprimer */
        .btn-delete {
            background: #ef4444;
            color: white;
            border: none;
            cursor: pointer;
        }

        /* Ajouter un espace entre les boutons */
        .btn-action + .btn-action {
            margin-left: 10px; /* Ajoute un peu d'espace entre les boutons */
        }

        /* Espacement entre les lignes du tableau */
        table tbody tr {
            border-bottom: 1px solid rgba(255, 255, 255, 0.2);  /* Ajoute une bordure sous chaque ligne */
            margin-bottom: 10px;  /* Ajoute un espacement entre chaque ligne */
        }

        /* Espacement entre les cellules */
        table td {
            padding: 6px 10px;  /* Augmenter l'espacement horizontal des cellules */
        }

        /* Espacement dans le tableau pour éviter que les lignes soient trop serrées */
        table {
            border-spacing: 0 12px;  /* Ajoute de l'espace entre les lignes du tableau */
        }

    </style>
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
            <a class="side-link" href="user">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm-7 9a7 7 0 0 1 14 0Z"/></svg>
                <span>Employés</span>
            </a>
            <a class="side-link active" href="poste.jsp">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 3v18M4 9l4-4 4 4m0 0l4-4 4 4" /></svg>
                <span>Postes</span>
            </a>
            <a class="side-link" href="projects">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 3h18v4H3Zm0 7h18v4H3Zm0 7h18v4H3Z"/></svg>
                <span>Projets</span>
            </a>
            <a class="side-link" href="departments">
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
                            <h2 class="welcome-title">Liste des postes</h2>
                            <p class="welcome-sub">Connecté en tant que Utilisateur</p>
                        </div>
                    </div>
                    <button style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out;" onclick="toggleAddModal(true)">+ Ajouter un poste</button>
                </div>

                <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
                    <table style="width:100%; border-collapse:collapse; color:#fff;">
                        <thead style="background:rgba(255,255,255,.15);">
                        <tr>
                            <th>ID</th>
                            <th>Nom du poste</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            // Affichage des postes statiques
                            int id = 1;
                            for (String post : posts) {
                        %>
                        <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
                            <td><%= id++ %></td>
                            <td><%= post %></td>
                            <td>
                                <!-- Bouton Modifier -->
                                <button type="button" class="btn-action btn-modify"
                                        onclick="toggleEditModal('<%= post %>', <%= id-1 %>)">
                                    Modifier
                                </button>

                                <!-- Formulaire pour supprimer le poste -->
                                <form method="post" action="poste.jsp" style="display:inline;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="<%= id %>">
                                    <button type="submit" class="btn-action btn-delete">Supprimer</button>
                                </form>
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

<!-- Modal Modifier Poste -->
<div id="modalEdit" class="modal-container">
    <div class="modal-box">
        <h3 style="color: #fff;">Modifier un poste</h3>
        <form method="post" action="poste.jsp">
            <label for="postName" style="color: white; font-weight: bold;">Nom du poste</label>
            <input id="postName" name="postName" class="input" placeholder="Nom du poste" required
                   style="background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 10px; padding: 10px; font-size: 1rem; backdrop-filter: blur(8px); width: 100%;">

            <button class="btn-save" name="action" value="update">Mettre à jour</button>
            <button type="button" class="btn-cancel" onclick="toggleEditModal(null)">Annuler</button>
        </form>
    </div>
</div>

<script src="assets/js/app.js"></script>
</body>
</html>
