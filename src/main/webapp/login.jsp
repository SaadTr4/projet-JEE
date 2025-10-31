<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Connexion - Gestion RH</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="assets/css/style.css" rel="stylesheet">
</head>
<body>

<!-- FOND -->
<div class="bg"></div>

<!-- FORMULAIRE DE CONNEXION -->
<div class="auth-card">
    <h4 class="auth-title">Connexion</h4>

    <form action="login" method="post">
        <div class="form-row">
            <label class="label" for="email">Adresse email</label>
            <input class="input" id="email" name="email" type="email" placeholder="ex: admin@jee.com" required>
        </div>

        <div class="form-row">
            <label class="label" for="password">Mot de passe</label>
            <input class="input" type="password" id="password" name="password" placeholder="••••••••" required>
        </div>

        <button class="btn" type="submit">Se connecter</button>
    </form>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert"><%= request.getAttribute("error") %></div>
    <% } %>

</div>

<footer class="footer">© 2025 - Application JEE Gestion RH</footer>

</body>
</html>
