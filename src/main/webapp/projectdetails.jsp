<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <link href="assets/css/projectdetails.css" rel="stylesheet" />
    <title>Détails du projet</title>
</head>
<body>
    <div class="bg"></div>

    <div class="project-details">
        <!-- Bouton Retour -->
        <button class="btn-back" onclick="window.location.href='projects'">Retour à la liste des projets</button>

        <!-- Titre du projet -->
        <h2>Détails du projet : Projet Alpha</h2>

        <!-- Description du projet -->
        <p class="project-description">
            <strong>Description :</strong>
            Développement d'une nouvelle application de gestion des tâches pour améliorer l'efficacité de l'équipe.
        </p>

        <!-- Chef de projet avec image -->
        <div class="project-manager-info">
            <img src="https://via.placeholder.com/150" alt="Photo du Chef de projet" class="profile-pic">
            <p class="project-manager">
                <strong>Chef de projet :</strong> Jean Dupont
            </p>
        </div>

        <!-- Statut -->
        <p class="project-status">
            <strong>Statut :</strong> En cours
        </p>

        <!-- Liste des employés affectés -->
        <div class="employees-assigned">
            <h3>Employés affectés au projet</h3>
            <table class="project-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nom</th>
                        <th>Email</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>EMP-001</td>
                        <td>Lucas Martin</td>
                        <td>lucas.martin@example.com</td>
                    </tr>
                    <tr>
                        <td>EMP-002</td>
                        <td>Claire Lefevre</td>
                        <td>claire.lefevre@example.com</td>
                    </tr>
                    <tr>
                        <td>EMP-003</td>
                        <td>Marc Bernard</td>
                        <td>marc.bernard@example.com</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Bouton pour assigner un nouvel employé -->
        <button class="btn-assign" onclick="window.location.href='assignEmployee.jsp?projectId=1'">Assigner un employé</button>
    </div>
</body>
</html>
