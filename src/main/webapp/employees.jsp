<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, fr.projetjee.model.User, fr.projetjee.enums.*, fr.projetjee.model.Department, fr.projetjee.model.Position" %>
<%@ page import="fr.projetjee.security.RolePermissions" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    String username = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Invité";

    List<User> users = (List<User>) request.getAttribute("users");
    List<Department> departments = (List<Department>) request.getAttribute("departments");
    List<Position> positions = (List<Position>) request.getAttribute("positions");
    String error = (String) request.getAttribute("error");

    Boolean searchActive = (Boolean) request.getAttribute("searchActive");
    Integer searchCount = (Integer) request.getAttribute("searchCount");
    String lastSearchDept = (String) request.getAttribute("lastSearchDept");
    String lastSearchPos = (String) request.getAttribute("lastSearchPos");
    String lastSearchRole = (String) request.getAttribute("lastSearchRole");
    String lastSearchGrade = (String) request.getAttribute("lastSearchGrade");
    String lastSearchText = (String) request.getAttribute("lastSearchText");

    boolean showFilter = true;
    boolean showAddButton = false;
    boolean showEditButton = false;
    boolean showDeleteButton = false;

    if (currentUser != null) {
        if (RolePermissions.isAdmin(currentUser) || RolePermissions.isDepartmentHeadRH(currentUser)) {
            showAddButton = true;
            showEditButton = true;
            showDeleteButton = true;
        } else if (RolePermissions.isEmployeRH(currentUser) || RolePermissions.isDepartmentHead(currentUser)) {
            showEditButton = true;
        }
    }
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
        /* Styles pour les champs désactivés dans les modals */
        .modal-input[readonly],
        .modal-input:disabled {
            background: rgba(255, 255, 255, 0.02); /* plus clair que le fond normal */
            color: rgba(255, 255, 255, 0.5);      /* texte grisé mais lisible */
            border-color: rgba(255, 255, 255, 0.1); /* bordure plus discrète */
            cursor: not-allowed;
            opacity: 0.8; /* léger effet d'opacité */
        }

        /* Optionnel : style pour les selects désactivés */
        .modal-input:disabled option {
            color: rgba(255, 255, 255, 0.5);
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
    </style>
</head>
<body>
<div class="bg"></div>

<div class="app-shell">
    <aside class="sidebar">
        <nav class="side-nav">
            <a class="side-link" href="dashboard.jsp">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M11 3 2 9v12h7v-7h6v7h7V9z"/></svg>
                <span>Tableau de bord</span>
            </a>

            <%
                // ✅ AFFICHER LE LIEN "EMPLOYÉS" SEULEMENT SI AUTORISÉ
                User sidebarUser = (User) session.getAttribute("currentUser");
                boolean showEmployeesLink = false;

                if (sidebarUser != null) {
                    String deptCodeSidebar = sidebarUser.getDepartment() != null ? sidebarUser.getDepartment().getCode() : "";
                    boolean isRHSidebar = sidebarUser.getRole() == Role.EMPLOYE && "RH".equalsIgnoreCase(deptCodeSidebar);

                    // Afficher pour : ADMIN, CHEF_DEPARTEMENT, CHEF_PROJET, EMPLOYE RH
                    showEmployeesLink = sidebarUser.getRole() == Role.ADMINISTRATEUR ||
                                       sidebarUser.getRole() == Role.CHEF_DEPARTEMENT ||
                                       sidebarUser.getRole() == Role.CHEF_PROJET ||
                                       isRHSidebar;
                }

                if (showEmployeesLink) {
            %>
            <a class="side-link <%= request.getRequestURI().contains("user") || request.getRequestURI().contains("employees") ? "active" : "" %>" href="user">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm-7 9a7 7 0 0 1 14 0Z"/></svg>
                <span>Employés</span>
            </a>
            <% } %>
            <a class="side-link" href="poste.jsp">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M12 3v18M4 9l4-4 4 4m0 0l4-4 4 4" /></svg>
                <span>Postes</span>
            </a>

            <a class="side-link <%= request.getRequestURI().contains("projects") ? "active" : "" %>" href="projects">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 3h18v4H3Zm0 7h18v4H3Zm0 7h18v4H3Z"/></svg>
                <span>Projets</span>
            </a>

            <a class="side-link <%= request.getRequestURI().contains("departments") ? "active" : "" %>" href="departments.jsp">
                <svg viewBox="0 0 24 24" width="18" height="18"><path fill="currentColor" d="M3 13h8V3H3Zm10 8h8V3h-8ZM3 21h8v-6H3Z"/></svg>
                <span>Départements</span>
            </a>

            <a class="side-link <%= request.getRequestURI().contains("payslips") ? "active" : "" %>" href="payslips">
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
                    <% if (showAddButton) { %>
                    <button class="welcome-logout" onclick="openAddModal()">+ Ajouter</button>
                    <% } %>
                </div>

                <% if (error != null) { %>
                <div class="error-msg"><%= error %></div>
                <% } %>

                <% if (showFilter) { %>
                <div class="search-card">
                    <div class="search-title">Recherche multicritère</div>
                    <form method="get" action="user">
                        <input type="hidden" name="action" value="search">
                        <div class="search-grid">
                            <div class="search-field">
                                <label>Nom, Prénom ou Matricule</label>
                                <input type="text" name="searchText" placeholder="Ex: Jean Dupont ou EMP0001" value="<%= lastSearchText != null ? lastSearchText : "" %>">
                            </div>
                            <div class="search-field">
                                <label>Département</label>
                                <select name="searchDepartment">
                                    <option value="all">Tous les départements</option>
                                    <% if (departments != null) {
                                        for (Department d : departments) {
                                            boolean selected = lastSearchDept != null && lastSearchDept.equals(String.valueOf(d.getId()));
                                    %>
                                    <option value="<%= d.getId() %>" <%= selected ? "selected" : "" %>><%= d.getName() %></option>
                                    <% } } %>
                                </select>
                            </div>
                            <div class="search-field">
                                <label>Poste</label>
                                <select name="searchPosition">
                                    <option value="all">Tous les postes</option>
                                    <% if (positions != null) {
                                        for (Position p : positions) {
                                            boolean selected = lastSearchPos != null && lastSearchPos.equals(String.valueOf(p.getId()));
                                    %>
                                    <option value="<%= p.getId() %>" <%= selected ? "selected" : "" %>><%= p.getName() %></option>
                                    <% } } %>
                                </select>
                            </div>
                            <div class="search-field">
                                <label>Rôle</label>
                                <select name="searchRole">
                                    <option value="all">Tous les rôles</option>
                                    <% for (Role r : Role.values()) {
                                        boolean selected = lastSearchRole != null && lastSearchRole.equals(r.name());
                                    %>
                                    <option value="<%= r.name() %>" <%= selected ? "selected" : "" %>><%= r %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="search-field">
                                <label>Grade</label>
                                <select name="searchGrade">
                                    <option value="all">Tous les grades</option>
                                    <% for (Grade g : Grade.values()) {
                                        boolean selected = lastSearchGrade != null && lastSearchGrade.equals(g.name());
                                    %>
                                    <option value="<%= g.name() %>" <%= selected ? "selected" : "" %>><%= g %></option>
                                    <% } %>
                                </select>
                            </div>
                        </div>
                        <div class="search-actions">
                            <button type="submit" class="btn-search btn-search-primary" style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: #fff; padding: 10px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: all 0.3s;">Rechercher</button>
                            <a href="user" class="btn-search btn-search-secondary" style="background: rgba(255,255,255,.1); color: #fff; padding: 10px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: all 0.3s; text-decoration: none;">Réinitialiser</a>
                        </div>
                    </form>
                </div>
                <% } %>

                <% if (searchActive != null && searchActive) { %>
                <div class="search-result-info">
                    <span style="font-size:1.5rem;"></span>
                    <div><strong><%= searchCount %> résultat(s)</strong> trouvé(s) pour votre recherche</div>
                </div>
                <% } %>

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
                            <% if (showEditButton || showDeleteButton) { %><th>Actions</th><% } %>
                        </tr>
                        </thead>
                        <tbody>
                        <% if (users != null && users.size() > 0) {
                            for (User u : users) { %>
                        <tr style="border-bottom:1px solid rgba(255,255,255,.2);">
                            <td style="padding:10px; text-align:center;">
                                <% if (u.getImage() != null) { %>
                                <img src="user?action=image&id=<%= u.getId() %>" class="profile-pic" alt="Photo <%= u.getFullName() %>">
                                <% } else { %>
                                <div class="no-pic">👤</div>
                                <% } %>
                            </td>
                            <td style="padding:10px;"><%= u.getMatricule() != null ? u.getMatricule() : "-" %></td>
                            <td><%= u.getFullName() %></td>
                            <td><%= u.getEmail() %></td>
                            <td><%= u.getRole().getDisplayName() %></td>
                            <td><%= u.getGrade() != null ? u.getGrade().getDisplayName() : "-" %></td>
                            <td><%= u.getDepartment() != null ? u.getDepartment().getName() : "-" %></td>
                            <td><%= u.getPosition() != null ? u.getPosition().getName() : "-" %></td>
                            <% if (showEditButton || showDeleteButton) { %>
                            <td style="padding:8px;">
                                <div style="display:flex; gap:10px;">
                                    <% if (showEditButton && RolePermissions.canViewPrivateInfo(currentUser, u)) { %>
                                    <button
                                            onclick='toggleUserEditModal({
                                                    id: <%= u.getId() %>,
                                                    lastName: "<%= StringEscapeUtils.escapeEcmaScript(u.getLastName()) %>",
                                                    firstName: "<%= StringEscapeUtils.escapeEcmaScript(u.getFirstName()) %>",
                                                    email: "<%= StringEscapeUtils.escapeEcmaScript(u.getEmail()) %>",
                                                    phone: "<%= u.getPhone() != null ? StringEscapeUtils.escapeEcmaScript(u.getPhone()) : "" %>",
                                                    address: "<%= u.getAddress() != null ? StringEscapeUtils.escapeEcmaScript(u.getAddress()) : "" %>",
                                                    role: "<%= u.getRole().name() %>",
                                                    grade: "<%= u.getGrade() != null ? u.getGrade().name() : "" %>",
                                                    departmentId: <%= u.getDepartment() != null ? u.getDepartment().getId() : "null" %>,
                                                    positionId: <%= u.getPosition() != null ? u.getPosition().getId() : "null" %>,
                                                    contractType: "<%= u.getContractType() != null ? u.getContractType().name() : "" %>",
                                                    baseSalary: <%= u.getBaseSalary() != null ? u.getBaseSalary() : 0 %>,
                                                    canEditPrivate: <%= RolePermissions.canUpdatePrivateInfo(currentUser, u) %>,
                                                    canEditPublic : <%= RolePermissions.canUpdatePublicInfo(currentUser, u) %>,
                                                    canEditSalary: <%= RolePermissions.canUpdateSalary(currentUser, u) %>,
                                                    isSelf: <%= u.getId().equals(currentUser.getId()) %>
                                                    })'
                                            class="welcome-logout"
                                            style="background: linear-gradient(135deg, #3b82f6, #06b6d4); color: white; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(59,130,246,.35); border: none; cursor: pointer; transition: filter 0.2s ease-in-out;">
                                        Modifier
                                    </button>

                                    <% } %>
                                    <% if (showDeleteButton && (RolePermissions.canDeleteUserWithTarget(currentUser,u)) ){ %>
                                    <form method="post" action="user" style="display:inline; margin:0;" onsubmit="return confirm('Confirmer la suppression de <%= u.getFullName().replace("'", "\\'") %> ?')">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="<%= u.getId() %>">
                                        <button type="submit" class="welcome-logout" style="background: #ef4444; color: #fff; font-weight: 600; border-radius: 10px; padding: 6px 10px; font-size: 0.9rem; box-shadow: 0 8px 22px rgba(239, 68, 68, .35); border: none; cursor: pointer; transition: filter 0.2s ease-in-out;"
                                        >Supprimer</button>
                                    </form>
                                    <% } %>
                                </div>
                            </td>
                            <% } %>
                        </tr>
                        <% }
                        } else { %>
                        <tr>
                            <td colspan="<%= (showEditButton || showDeleteButton) ? "9" : "8" %>" style="padding:40px; text-align:center; color:rgba(255,255,255,0.6);">
                                <div style="font-size:3rem; margin-bottom:16px;">🔍</div>
                                <div style="font-size:1.1rem;">Aucun employé trouvé</div>
                                <% if (searchActive != null && searchActive) { %>
                                <a href="user" style="color:#667eea; margin-top:12px; display:inline-block;">Afficher tous les employés</a>
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

<% if (showAddButton) { %>
<div id="modalAdd" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.7); z-index:999; justify-content:center; align-items:center; overflow-y:auto; padding:20px;">
    <div style="background:rgba(255,255,255,.15); padding:24px; border-radius:12px; backdrop-filter:blur(10px); width:680px; max-width:95%; border:1px solid rgba(255,255,255,.1);">
        <h3 style="margin-bottom:20px; color:#fff;">Ajouter un employé</h3>
        <form method="post" action="user" enctype="multipart/form-data">
            <div class="form-col-2">
                <div><label style="color:white; font-size:0.9rem;">Nom *</label><input name="nom" class="modal-input" placeholder="Nom de famille" required style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"></div>
                <div><label style="color:white; font-size:0.9rem;">Prénom *</label><input name="prenom" class="modal-input" placeholder="Prénom" required style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"></div>
                <div><label style="color:white; font-size:0.9rem;">Email *</label><input name="email" type="email" class="modal-input" placeholder="exemple@mail.com" required style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"></div>
                <div><label style="color:white; font-size:0.9rem;">Téléphone</label><input name="phone" class="modal-input" placeholder="+33 6 12 34 56 78" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"></div>
                <div class="form-col-1"><label  style="color:white; font-size:0.9rem;">Adresse</label><input name="address" class="modal-input" placeholder="Adresse complète" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"></div>
                <div class="form-col-1"><label  style="color:white; font-size:0.9rem;">Photo de profil (optionnel)</label><input type="file" name="image" accept="image/*" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"><small style="color:rgba(255,255,255,0.6); display:block; margin-top:4px;">Formats acceptés : JPG, PNG (5MB max)</small></div>
                <div><label  style="color:white; font-size:0.9rem;">Rôle *</label><select name="role" class="modal-input" required style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"><option value="">-- Sélectionner un rôle --</option><% for (Role r : Role.values()) { %><option value="<%= r.name() %>"><%= r.getDisplayName() %></option><% } %></select></div>
                <div><label  style="color:white; font-size:0.9rem;">Grade</label><select name="grade" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"><option value="">-- Sélectionner un grade --</option><% for (Grade g : Grade.values()) { %><option value="<%= g.name() %>"><%= g.getDisplayName() %></option><% } %></select></div>
                <div><label style="color:white; font-size:0.9rem;">Département</label><select name="department" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"><option value="">-- Sélectionner un département --</option><% if (departments != null) for (Department d : departments) { %><option value="<%= d.getId() %>"><%= d.getName() %></option><% } %></select></div>
                <div><label style="color:white; font-size:0.9rem;">Poste</label><select name="position" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"><option value="">-- Sélectionner un poste --</option><% if (positions != null) for (Position p : positions) { %><option value="<%= p.getId() %>"><%= p.getName() %></option><% } %></select></div>
                <div><label style="color:white; font-size:0.9rem;">Type de contrat</label><select name="typeContrat" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"><% for (ContractType ct : ContractType.values()) { %><option value="<%= ct.name() %>"><%= ct.getDisplayName() %></option><% } %></select></div>
                <div><label style="color:white; font-size:0.9rem;">Salaire</label><input type="number" name="salaire" class="modal-input" step="0.01" placeholder="Ex: 2500.00" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;"></div>
            </div>
            <div style="display:flex; gap:12px; margin-top:20px;">
                <button type="submit" class="welcome-logout" style="flex:1; background:linear-gradient(135deg, #3b82f6, #06b6d4); color:white; padding:14px 16px; font-size:1rem; border:none; border-radius:8px; cursor:pointer;">Enregistrer</button>
                <button type="button" class="welcome-logout" style="flex:1; background:#ef4444; color:white; padding:14px 16px; font-size:1rem; border:none; border-radius:8px; cursor:pointer;" onclick="closeAddModal()">Annuler</button>
            </div>
        </form>
    </div>
</div>
<% } %>

<% if (showEditButton) { %>
<div id="modalEdit" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,.7); z-index:999; justify-content:center; align-items:center; overflow-y:auto; padding:20px;">
    <div  style="background:rgba(255,255,255,.15);padding:24px; border-radius:12px; backdrop-filter:blur(10px); width:680px; max-width:95%; border:1px solid rgba(255,255,255,.1);">
        <h3 style="margin-bottom:20px; color:#fff;">Modifier un employé</h3>
        <form method="post" action="user" enctype="multipart/form-data">
            <input type="hidden" id="edit_id" name="id">
            <div class="form-col-2">
                <div>
                    <label style="color:white; font-size:0.9rem;">Nom *</label>
                    <input id="edit_nom" name="nom" class="modal-input" required style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>
                <div>
                    <label style="color:white; font-size:0.9rem;">Prénom *</label>
                    <input  id="edit_prenom" name="prenom" class="modal-input" required style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>
                <div>
                    <label style="color:white; font-size:0.9rem;">Email *</label>
                    <input id="edit_email" name="email" type="email" class="modal-input" required style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>
                <div>
                    <label style="color:white; font-size:0.9rem;">Téléphone</label>
                    <input id="edit_phone" name="phone" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>
                <div class="form-col-1">
                    <label style="color:white; font-size:0.9rem;">Adresse</label>
                    <input id="edit_address" name="address" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                </div>
                <div class="form-col-1">
                    <label style="color:white; font-size:0.9rem;">Changer la photo de profil (optionnel)</label>
                    <input type="file" name="image" accept="image/*" class="modal-input">
                    <small style="color:rgba(255,255,255,0.6); display:block; margin-top:4px;">
                        Laisser vide pour conserver l'image actuelle
                    </small>
                </div>
                <div>
                    <label style="color:white; font-size:0.9rem;">Rôle *</label>
                    <select id="edit_role" name="role" class="modal-input" required style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un rôle --</option>
                        <% for (Role r : Role.values()) { %>
                        <option value="<%= r.name() %>"><%= r.getDisplayName() %></option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label style="color:white; font-size:0.9rem;">Grade</label>
                    <select id="edit_grade" name="grade" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un grade --</option>
                        <% for (Grade g : Grade.values()) { %>
                        <option value="<%= g.name() %>"><%= g.getDisplayName() %></option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label>Département</label>
                    <select id="edit_department" name="department" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un département --</option>
                        <% if (departments != null) for (Department d : departments) { %>
                        <option value="<%= d.getId() %>"><%= d.getName() %></option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label style="color:white; font-size:0.9rem;">Poste</label>
                    <select id="edit_position" name="position" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <option value="">-- Sélectionner un poste --</option>
                        <% if (positions != null) for (Position p : positions) { %>
                        <option value="<%= p.getId() %>"><%= p.getName() %></option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label style="color:white; font-size:0.9rem;">Type de contrat</label>
                    <select id="edit_typeContrat" name="typeContrat" class="modal-input" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;">
                        <% for (ContractType ct : ContractType.values()) { %>
                        <option value="<%= ct.name() %>"><%= ct.getDisplayName() %></option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label style="color:white; font-size:0.9rem;">Salaire</label>
                    <input id="edit_salaire" type="number" name="salaire" class="modal-input" step="0.01" placeholder="Ex: 2500.00" style="width:100%; max-width:95%; padding:14px; margin-top:8px; border-radius:8px; border:1px solid rgba(255,255,255,.3); background:rgba(255,255,255,.1); color:#fff;" >
                </div>
            </div>
            <div style="display:flex; gap:12px; margin-top:20px;">
                <button type="submit" class="welcome-logout" style="flex:1; background:linear-gradient(135deg, #3b82f6, #06b6d4); color:white; padding:14px 16px; font-size:1rem; border:none; border-radius:8px; cursor:pointer;">Mettre à jour</button>
                <a href="user" class="welcome-logout" style="flex:1; background:#ef4444; color:white; padding:14px 16px; font-size:1rem; border:none; border-radius:8px; cursor:pointer;">
                    Annuler
                </a>
            </div>
        </form>
    </div>
</div>
<% } %>

<script src="assets/js/app.js"></script>
</body>
</html>