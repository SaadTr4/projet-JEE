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
        <button class="btn-back" onclick="window.location.href='departments'">Retour à la liste des départements</button>

        <!-- Titre du département -->
        <h2>Détails du départements : Département Alpha</h2>

        <!-- Description du département -->
        <p class="project-description">
            <strong>Description :</strong>
            Développement d'une nouvelle application de gestion des tâches pour améliorer l'efficacité de l'équipe.
        </p>

        <!-- Chef de Département avec image -->
        <div class="project-manager-info">
            <img src="https://via.placeholder.com/150" alt="Photo du Chef de Departements" class="profile-pic">
            <p class="project-manager">
                <strong>Chef de département :</strong> Jean Dupont
            </p>
        </div>

        <!-- Liste des employés affectés -->
        <div class="employees-assigned">
            <h3>Employés affectés au département</h3>
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
        <button class="btn-assign" onclick="window.location.href='assignEmployee.jsp?projectId=1'">Assigner un département</button>
    </div>
</body>
</html>
