<%@ page import="fr.projetjee.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Profil</title>
    <link rel="stylesheet" href="assets/css/profile.css">
</head>

<body>

<div class="bg"></div>

<div class="profile-card">

    <div class="profile-photo">
        <img src="profile-image" alt="Photo de profil">
    </div>

    <h2 class="name"><%= currentUser.getFullName() %></h2>
    <p class="role"><%= currentUser.getRole() != null ? currentUser.getRole() : "Employé" %></p>

    <div class="info">
        <p><strong>Matricule :</strong> <%= currentUser.getMatricule() %></p>
        <p><strong>Email :</strong> <%= currentUser.getEmail() %></p>
        <p><strong>Téléphone :</strong> <%= currentUser.getPhone() != null ? currentUser.getPhone() : "Non renseigné" %></p>
        <p><strong>Adresse :</strong> <%= currentUser.getAddress() != null ? currentUser.getAddress() : "Non renseignée" %></p>
    </div>

    <a href="dashboard.jsp" class="btn-back">Retour</a>

</div>

</body>
</html>
