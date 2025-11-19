# projet-JEE
Application JEE/Jakarta EE pour la gestion des employés, départements, projets et fiches de paie.
# JEE-projet-groupe5

## Objectif du projet
Développer une application web JEE (Jakarta EE) permettant de gérer :
- les employés (ajout, modification, suppression, affectation),
- les départements (création, affectation des membres),
- les projets (suivi d’avancement, affectation d’employés),
- les fiches de paie (calcul automatique du salaire net mensuel).

Ce projet s’inscrit dans le cadre du module J2EE (ING2 GSI) – Année universitaire 2025-2026.  
L’application devra également être refaite en Spring Boot dans une deuxième version.

---

## Équipe
**Groupe 5 – CyTech GSI**

| Nom | Rôle | Technologie principale |
|------|------|------------------------|

---

## Technologies utilisées
| Composant | Outil / Technologie                                     |
|------------|---------------------------------------------------------|
| Langage | Java 21                                                 |
| Framework Web | Jakarta EE (Servlets, JSP, JSTL)                        |
| Serveur | Apache Tomcat 10                                        |
| Base de données | PostgreSQL                                              |
| ORM | Hibernate (JPA)                                         |
| IDE | Eclipse IDE for Enterprise Developers and IntelliJ IDEA |
| Gestion de version | Git et GitHub                                           |
| Frontend | HTML, CSS, JavaScript                                   |

---

## Installation et exécution du projet

## 1. Cloner le dépôt
```bash
git clone https://github.com/SaadTr4/JEE-projet-groupe5.git
cd JEE-projet-groupe5
```

## 2. Lancement de la base de données et des conteneurs Docker

### 1️⃣ Démarrer les conteneurs
Depuis la racine du projet :

```bash
docker compose -f docker/docker-compose.yml up -d
```


### 2️⃣ Accéder à la base de données PostgreSQL
Depuis un terminal Docker :
```bash
docker exec -it projetjee-db psql -U cytech_user -d cytech_entreprise
```
Depuis un client externe : 
```bash
psql -h localhost -p 5433 -U cytech_user -d cytech_entreprise
```


### 3️⃣ Arrêter les conteneurs
Pour arrêter le conteneur, exécute :
```bash
docker compose -f docker/docker-compose.yml down
```

### 4️⃣ Reconstruire la base de données 
Pour supprimer toutes les données et reconstruire la base de données, exécute :
```bash
docker compose -f docker/docker-compose.yml down
docker volume rm docker_jee-dev_postgres_data # utiliser cette commande pour supprimer le volume de données (non obligatoire)
docker compose -f docker/docker-compose.yml up -d
```

## 3. Se connecter à pgAdmin
Ouvre un navigateur et accède à : `http://localhost:8081`
Utilise les identifiants suivants :
- mail : admin@admin.com
- mot de passe : admin

Se connecter au serveur PostgreSQL :
- Hôte : db
- Port : 5432
- Maintenance DB : cytech_entreprise
- Nom d’utilisateur : cytech_user
- Mot de passe : CyT3ch2025!
- 