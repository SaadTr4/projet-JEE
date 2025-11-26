<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.*, fr.projetjee.model.User, fr.projetjee.enums.*, fr.projetjee.model.Department, fr.projetjee.model.Position" %>

<%
    String username = "Admin";
    List<User> users = (List<User>) request.getAttribute("users");
    List<Department> departments = (List<Department>) request.getAttribute("departments");
    List<Position> positions = (List<Position>) request.getAttribute("positions");
    User userEdit = (User) request.getAttribute("userEdit");
    Boolean editMode = (Boolean) request.getAttribute("editMode");
    String error = (String) request.getAttribute("error");

    // Paramètres de recherche
    Boolean searchActive = (Boolean) request.getAttribute("searchActive");
    Integer searchCount = (Integer) request.getAttribute("searchCount");
    String lastSearchDept = (String) request.getAttribute("lastSearchDept");
    String lastSearchPos = (String) request.getAttribute("lastSearchPos");
    String lastSearchRole = (String) request.getAttribute("lastSearchRole");
    String lastSearchGrade = (String) request.getAttribute("lastSearchGrade");
    String lastSearchText = (String) request.getAttribute("lastSearchText");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Employés - Gestion RH</title>
    <link href="assets/css/dashboard.css" rel="stylesheet">
    <style>
        .modal-input {
            width:100%;
            padding:10px;
            margin-top:8px;
            border-radius:8px;
            border:1px solid rgba(255,255,255,.3);
            background:rgba(255,255,255,.05);
            color:#fff;
        }
        .form-row {
            display:flex;
            gap:16px;
            margin-bottom:12px;
        }
        .form-row > div {
            flex:1;
        }
        .form-col-2 {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }
        .form-col-1 {
            grid-column: 1 / -1;
        }
        label {
            display: block;
            font-size: 0.9rem;
            margin-bottom: 4px;
            color: rgba(255,255,255,0.8);
        }
        .profile-pic {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            object-fit: cover;
        }
        .no-pic {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: rgba(255,255,255,0.1);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
        }
        .error-msg {
            background: #ef4444;
            color: #fff;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 16px;
        }

        /* ===== STYLES RECHERCHE ===== */
        .search-card {
            background: rgba(255,255,255,.05);
            backdrop-filter: blur(10px);
            border-radius: 16px;
            padding: 20px;
            margin-bottom: 20px;
            border: 1px solid rgba(255,255,255,.1);
        }
        .search-title {
            font-size: 1.1rem;
            font-weight: 600;
            margin-bottom: 16px;
            color: #fff;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .search-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 12px;
            margin-bottom: 16px;
        }
        .search-field label {
            font-size: 0.85rem;
            color: rgba(255,255,255,0.7);
            margin-bottom: 6px;
            display: block;
        }
        .search-field select,
        .search-field input {
            width: 100%;
            padding: 10px;
            border-radius: 8px;
            border: 1px solid rgba(255,255,255,.3);
            background: rgba(255,255,255,.1);
            color: #fff;
            font-size: 0.9rem;
        }
        .search-field select option {
            background: #1a1a2e;
            color: #fff;
        }
        .search-actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
        }
        .btn-search {
            padding: 10px 24px;
            border-radius: 8px;
            border: none;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 8px;
            transition: all 0.3s;
        }
        .btn-search-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
        }
        .btn-search-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
        }
        .btn-search-secondary {
            background: rgba(255,255,255,.1);
            color: #fff;
        }
        .btn-search-secondary:hover {
            background: rgba(255,255,255,.2);
        }
        .search-result-info {
            background: rgba(102, 126, 234, 0.2);
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 16px;
            display: flex;
            align-items: center;
            gap: 12px;
            border-left: 4px solid #667eea;
        }
        .search-result-info strong {
            color: #fff;
        }
        .search-grid {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr 1fr; /* 4 colonnes égales */
            gap: 16px; /* Espacement entre les filtres */
            margin-bottom: 16px;
        }

        .search-field {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }

        .search-field label {
            font-size: 0.85rem;
            color: rgba(255,255,255,0.7);
            margin-bottom: 6px;
            display: block;
        }

        .search-field input,
        .search-field select {
            width: 100%;
            padding: 10px;
            border-radius: 8px;
            border: 1px solid rgba(255,255,255,.3);
            background: rgba(255,255,255,.1);
            color: #fff;
            font-size: 0.9rem;
        }

        /* Réduction des deux premiers filtres */
        .search-field:nth-child(1),
        .search-field:nth-child(2) {
            width: auto; /* Enlever la largeur de 100% pour les ajuster à un plus petit espace */
            max-width: 250px; /* Limiter la largeur des deux premiers filtres */
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

            <a class="side-link active" href="user">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm-7 9a7 7 0 0 1 14 0Z"/></svg>
                <span>Employés</span>
            </a>
            <a class="side-link" href="poste.jsp">
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
                        <span class="wave"></span>
                        <div>
                            <h2 class="welcome-title">Liste des employés</h2>
                            <p class="welcome-sub">Connecté en tant que <%= username %></p>
                        </div>
                    </div>

                    <button class="welcome-logout" onclick="openAddModal()" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #e0f2fe; font-weight: 600; border-radius: 10px; padding: 10px 14px; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; font-size: 1rem; transition: filter 0.2s ease-in-out;">+ Ajouter</button>

                </div>

                <% if (error != null) { %>
                <div class="error-msg"> <%= error %></div>
                <% } %>

                <!-- ==================== FORMULAIRE DE RECHERCHE ==================== -->
                <div class="search-card">
                    <div class="search-title">
                        Recherche multicritère
                    </div>

                    <form method="get" action="user">
                        <input type="hidden" name="action" value="search">

                        <div class="search-grid">
                            <!-- Recherche texte -->
                            <div class="search-field">
                                <label>Nom, Prénom ou Matricule</label>
                                <input type="text"
                                       name="searchText"
                                       placeholder="Ex: Jean Dupont ou EMP-0001"
                                       value="<%= lastSearchText != null ? lastSearchText : "" %>">
                            </div>

                            <!-- Département -->
                            <div class="search-field">
                                <label>Département</label>
                                <select name="searchDepartment">
                                    <option value="all">Tous les départements</option>
                                    <% if (departments != null) {
                                        for (Department d : departments) {
                                            boolean selected = lastSearchDept != null && lastSearchDept.equals(String.valueOf(d.getId()));
                                    %>
                                    <option value="<%= d.getId() %>" <%= selected ? "selected" : "" %>>
                                        <%= d.getName() %>
                                    </option>
                                    <% } } %>
                                </select>
                            </div>

                            <!-- Poste -->
                            <div class="search-field">
                                <label>Poste</label>
                                <select name="searchPosition">
                                    <option value="all">Tous les postes</option>
                                    <% if (positions != null) {
                                        for (Position p : positions) {
                                            boolean selected = lastSearchPos != null && lastSearchPos.equals(String.valueOf(p.getId()));
                                    %>
                                    <option value="<%= p.getId() %>" <%= selected ? "selected" : "" %>>
                                        <%= p.getName() %>
                                    </option>
                                    <% } } %>
                                </select>
                            </div>

                            <!-- Rôle -->
                            <div class="search-field">
                                <label>Rôle</label>
                                <select name="searchRole">
                                    <option value="all">Tous les rôles</option>
                                    <% for (Role r : Role.values()) {
                                        boolean selected = lastSearchRole != null && lastSearchRole.equals(r.name());
                                    %>
                                    <option value="<%= r.name() %>" <%= selected ? "selected" : "" %>>
                                        <%= r %>
                                    </option>
                                    <% } %>
                                </select>
                            </div>

                            <!-- Grade -->
                            <div class="search-field">
                                <label>Grade</label>
                                <select name="searchGrade">
                                    <option value="all">Tous les grades</option>
                                    <% for (Grade g : Grade.values()) {
                                        boolean selected = lastSearchGrade != null && lastSearchGrade.equals(g.name());
                                    %>
                                    <option value="<%= g.name() %>" <%= selected ? "selected" : "" %>>
                                        <%= g %>
                                    </option>
                                    <% } %>
                                </select>
                            </div>
                        </div>

                        <div class="search-actions">
                            <button type="submit" class="btn-search btn-search-primary" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #fff; padding: 10px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: all 0.3s;">
                                 Rechercher
                            </button>

                            <a href="user" class="btn-search btn-search-secondary" style="background: rgba(255,255,255,.1); color: #fff; padding: 10px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: all 0.3s; text-decoration: none;">
                                 Réinitialiser
                            </a>
                        </div>
                    </form>
                </div>

                <!-- Résultat de recherche -->
                <% if (searchActive != null && searchActive) { %>
                <div class="search-result-info">
                    <span style="font-size:1.5rem;"></span>
                    <div>
                        <strong><%= searchCount %> résultat(s)</strong> trouvé(s) pour votre recherche
                    </div>
                </div>
                <% } %>

                <!-- ==================== TABLEAU DES EMPLOYÉS ==================== -->
                <div class="chart-card" style="overflow-x:auto; margin-top:16px;">
                    <table style="width:100%; border-collapse:collapse; color:#fff;">
                        <thead style="background:rgba(255,255,255,.15);">
                        <tr>
                            <th style="padding:10px;">Photo</th>
                            <th>Matricule</th>
                            <th>Nom complet</th>
                            <th>Email</th>
                            <th>Rôle</th>
                            <th>Grade</th>
                            <th>Département</th>
                            <th>Poste</th>
                            <th>Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        <% if (users != null && users.size() > 0) {
                            for (User u : users) { %>

                        <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
                            <td style="padding:10px; text-align:center;">
                                <% if (u.getImage() != null) { %>
                                <img src="user?action=image&id=<%= u.getId() %>"
                                     class="profile-pic"
                                     alt="Photo <%= u.getFullName() %>">
                                <% } else { %>
                                <div class="no-pic">👤</div>
                                <% } %>
                            </td>
                            <td style="padding:10px;"><%= u.getMatricule() != null ? u.getMatricule() : "-" %></td>
                            <td><%= u.getFullName() %></td>
                            <td><%= u.getEmail() %></td>
                            <td><%= u.getRole().getDisplayName() %></td>
                            <td><%= u.getGrade().getDisplayName() != null ? u.getGrade().getDisplayName() : "-" %></td>
                            <td><%= u.getDepartment() != null ? u.getDepartment().getName() : "-" %></td>
                            <td><%= u.getPosition() != null ? u.getPosition().getName() : "-" %></td>

                            <td style="padding:8px;">
                                <div style="display:flex; gap:10px;">
                                    <button onclick="openEditModal(<%= u.getId() %>, '<%= u.getLastName() %>', '<%= u.getFirstName() %>', '<%= u.getEmail() %>', '<%= u.getPhone() != null ? u.getPhone() : "" %>', '<%= u.getAddress() != null ? u.getAddress() : "" %>', '<%= u.getRole().name() %>', '<%= u.getGrade() != null ? u.getGrade().name() : "" %>', <%= u.getDepartment() != null ? u.getDepartment().getId() : "null" %>, <%= u.getPosition() != null ? u.getPosition().getId() : "null" %>)"
                                            class="welcome-logout"
                                            style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: white; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; transition: filter 0.2s ease-in-out;">
                                        Modifier
                                    </button>

                                    <a href="user?action=delete&id=<%= u.getId() %>"
                                               class="welcome-logout"
                                               style="background: #ef4444; color: #fff; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(239, 68, 68, .35); border: none; cursor: pointer; transition: filter 0.2s ease-in-out;"
                                               onclick="return confirm('Confirmer la suppression de <%= u.getFullName() %> ?')">
                                                Supprimer
                                    </a>
                                </div>
                            </td>
                        </tr>

                        <% }
                        } else { %>
                        <tr>
                            <td colspan="9" style="padding:40px; text-align:center; color:rgba(255,255,255,0.6);">
                                <div style="font-size:3rem; margin-bottom:16px;">🔍</div>
                                <div style="font-size:1.1rem;">Aucun employé trouvé</div>
                                <% if (searchActive != null && searchActive) { %>
                                <a href="user" style="color:#667eea; margin-top:12px; display:inline-block;">
                                    Afficher tous les employés
                                </a>
                                <% } %>
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

<!-- ==================== MODAL AJOUT ==================== -->
<div id="modalAdd" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.7); z-index:999; justify-content:center; align-items:center; overflow-y:auto; padding:20px;">
    <div style="background:rgba(255,255,255,.15); padding:24px; border-radius:12px; backdrop-filter:blur(10px); width:680px; max-width:95%; border:1px solid rgba(255,255,255,.1);">
        <h3 style="margin-bottom:20px; color:#fff;">Ajouter un employé</h3>

        <form method="post" action="user" enctype="multipart/form-data">

            <div class="form-col-2">
                <!-- Nom -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Nom *</label>
                    <input name="nom" class="modal-input" placeholder="Nom de famille" required
                           style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>

                <!-- Prénom -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Prénom *</label>
                    <input name="prenom" class="modal-input" placeholder="Prénom" required
                           style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>

                <!-- Email -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Email *</label>
                    <input name="email" type="email" class="modal-input" placeholder="exemple@mail.com" required
                           style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>

                <!-- Téléphone -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Téléphone</label>
                    <input name="phone" class="modal-input" placeholder="+33 6 12 34 56 78"
                           style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>
            </div>

            <!-- Adresse -->
            <div class="form-col-1">
                <label style="color:white; font-size:0.9rem;">Adresse</label>
                <input name="address" class="modal-input" placeholder="Adresse complète"
                       style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
            </div>

            <!-- Photo de profil (optionnel) -->
            <div class="form-col-1">
                <label style="color:white; font-size:0.9rem;">Photo de profil (optionnel)</label>
                <input type="file" name="image" accept="image/*" class="modal-input"
                       style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                <small style="color:rgba(255,255,255,0.6); display:block; margin-top:4px;">Formats acceptés : JPG, PNG (5MB max)</small>
            </div>

            <div class="form-col-2">
                <!-- Rôle -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Rôle *</label>
                    <select name="role" class="modal-input" required
                            style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff; font-size:0.9rem;">
                        <option value="">-- Sélectionner un rôle --</option>
                        <% for (Role r : Role.values()) { %>
                            <option value="<%= r.name() %>"><%= r %></option>
                        <% } %>
                    </select>
                </div>

                <!-- Grade -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Grade</label>
                    <select name="grade" class="modal-input"
                            style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un grade --</option>
                        <% for (Grade g : Grade.values()) { %>
                            <option value="<%= g.name() %>"><%= g %></option>
                        <% } %>
                    </select>
                </div>
            </div>

            <div class="form-col-2">
                <!-- Département -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Département</label>
                    <select name="department" class="modal-input"
                            style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un département --</option>
                        <% if (departments != null)
                            for (Department d : departments) { %>
                            <option value="<%= d.getId() %>"><%= d.getName() %></option>
                        <% } %>
                    </select>
                </div>

                <!-- Poste -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Poste</label>
                    <select name="position" class="modal-input"
                            style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un poste --</option>
                        <% if (positions != null)
                            for (Position p : positions) { %>
                            <option value="<%= p.getId() %>"><%= p.getName() %></option>
                        <% } %>
                    </select>
                </div>
            </div>

            <div style="display:flex; gap:12px; margin-top:20px;">
                <button type="submit" class="welcome-logout"
                        style="flex:1; background:linear-gradient(135deg, #3b82f6, #06b6d4); color:white; padding:14px 16px; font-size:1rem; border:none; border-radius:8px; cursor:pointer;">
                     Enregistrer
                </button>
                <button type="button" class="welcome-logout"
                        style="flex:1; background:#ef4444; color:white; padding:14px 16px; font-size:1rem; border:none; border-radius:8px; cursor:pointer;"
                        onclick="closeAddModal()"> Annuler</button>
            </div>
        </form>
    </div>
</div>

<!-- ==================== MODAL ÉDITION ==================== -->
<div id="modalEdit" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.7); z-index:999; justify-content:center; align-items:center; overflow-y:auto; padding:20px;">
    <div style="background:rgba(255,255,255,.15); padding:24px; border-radius:12px; backdrop-filter:blur(10px); width:680px; max-width:95%; border:1px solid rgba(255,255,255,.1);">
        <h3 style="margin-bottom:20px; color:#fff;">Modifier un employé</h3>

        <form method="post" action="user" enctype="multipart/form-data">
            <input type="hidden" name="id" id="edit_id">

            <div class="form-col-2">
                <!-- Nom -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Nom *</label>
                    <input name="nom" id="edit_nom" class="modal-input" required
                           style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>

                <!-- Prénom -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Prénom *</label>
                    <input name="prenom" id="edit_prenom" class="modal-input" required
                           style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>

                <!-- Email -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Email *</label>
                    <input name="email" id="edit_email" type="email" class="modal-input" required
                           style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>

                <!-- Téléphone -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Téléphone</label>
                    <input name="phone" id="edit_phone" class="modal-input"
                           style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>
            </div>

            <!-- Adresse -->
            <div class="form-col-1">
                <label style="color:white; font-size:0.9rem;">Adresse</label>
                <input name="address" id="edit_address" class="modal-input"
                       style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
            </div>

            <!-- Photo de profil (optionnel) -->
            <div class="form-col-1">
                <label style="color:white; font-size:0.9rem;">Changer la photo de profil (optionnel)</label>
                <input type="file" name="image" accept="image/*" class="modal-input"
                       style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                <small style="color:rgba(255,255,255,0.6); display:block; margin-top:4px;">Laisser vide pour conserver l'image actuelle</small>
            </div>

            <div class="form-col-2">
                <!-- Rôle -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Rôle *</label>
                    <select name="role" id="edit_role" class="modal-input" required
                            style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff; font-size:0.9rem;">
                        <option value="">-- Sélectionner un rôle --</option>
                        <% for (Role r : Role.values()) { %>
                            <option value="<%= r.name() %>"><%= r %></option>
                        <% } %>
                    </select>
                </div>

                <!-- Grade -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Grade</label>
                    <select name="grade" id="edit_grade" class="modal-input"
                            style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un grade --</option>
                        <% for (Grade g : Grade.values()) { %>
                            <option value="<%= g.name() %>"><%= g %></option>
                        <% } %>
                    </select>
                </div>
            </div>

            <div class="form-col-2">
                <!-- Département -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Département</label>
                    <select name="department" id="edit_department" class="modal-input"
                            style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un département --</option>
                        <% if (departments != null)
                            for (Department d : departments) { %>
                            <option value="<%= d.getId() %>"><%= d.getName() %></option>
                        <% } %>
                    </select>
                </div>

                <!-- Poste -->
                <div>
                    <label style="color:white; font-size:0.9rem;">Poste</label>
                    <select name="position" id="edit_position" class="modal-input"
                            style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un poste --</option>
                        <% if (positions != null)
                            for (Position p : positions) { %>
                            <option value="<%= p.getId() %>"><%= p.getName() %></option>
                        <% } %>
                    </select>
                </div>
            </div>

            <div style="display:flex; gap:12px; margin-top:20px;">
                <button type="submit" class="welcome-logout"
                        style="flex:1; background:linear-gradient(135deg, #3b82f6, #06b6d4); color:white; padding:14px 16px; font-size:1rem; border:none; border-radius:8px; cursor:pointer;">
                     Mettre à jour
                </button>
                <button type="button" class="welcome-logout"
                        style="flex:1; background:#ef4444; color:white; padding:14px 16px; font-size:1rem; border:none; border-radius:8px; cursor:pointer;"
                        onclick="closeEditModal()"> Annuler</button>
            </div>
        </form>
    </div>
</div>


<script>
    // ===== Modal AJOUT =====
    function openAddModal() {
        document.getElementById('modalAdd').style.display = 'flex';
    }

    function closeAddModal() {
        document.getElementById('modalAdd').style.display = 'none';
    }

    // ===== Modal ÉDITION =====
    function openEditModal(id, nom, prenom, email, phone, address, role, grade, deptId, posId) {
        document.getElementById('edit_id').value = id;
        document.getElementById('edit_nom').value = nom;
        document.getElementById('edit_prenom').value = prenom;
        document.getElementById('edit_email').value = email;
        document.getElementById('edit_phone').value = phone;
        document.getElementById('edit_address').value = address;
        document.getElementById('edit_role').value = role;
        document.getElementById('edit_grade').value = grade || '';
        document.getElementById('edit_department').value = deptId || '';
        document.getElementById('edit_position').value = posId || '';

        document.getElementById('modalEdit').style.display = 'flex';
    }

    function closeEditModal() {
        document.getElementById('modalEdit').style.display = 'none';
    }

    // Auto-ouvrir le modal d'édition si editMode est actif
    <% if (editMode != null && editMode && userEdit != null) { %>
    window.addEventListener('DOMContentLoaded', function() {
        openEditModal(
            <%= userEdit.getId() %>,
            '<%= userEdit.getLastName() %>',
            '<%= userEdit.getFirstName() %>',
            '<%= userEdit.getEmail() %>',
            '<%= userEdit.getPhone() != null ? userEdit.getPhone() : "" %>',
            '<%= userEdit.getAddress() != null ? userEdit.getAddress() : "" %>',
            '<%= userEdit.getRole().name() %>',
            '<%= userEdit.getGrade() != null ? userEdit.getGrade().name() : "" %>',
            <%= userEdit.getDepartment() != null ? userEdit.getDepartment().getId() : "null" %>,
            <%= userEdit.getPosition() != null ? userEdit.getPosition().getId() : "null" %>
        );
    });
    <% } %>
</script>

</body>
</html>