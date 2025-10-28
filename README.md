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
| Composant | Outil / Technologie |
|------------|---------------------|
| Langage | Java 21 |
| Framework Web | Jakarta EE (Servlets, JSP, JSTL) |
| Serveur | Apache Tomcat 9 ou 10 |
| Base de données | MySQL |
| ORM | Hibernate (JPA) |
| IDE | Eclipse IDE for Enterprise Developers |
| Gestion de version | Git et GitHub |
| Frontend | HTML, CSS, JavaScript |

---

## Installation et exécution du projet

### 1. Cloner le dépôt
```bash

git clone https://github.com/SaadTr4/JEE-projet-groupe5.git
cd JEE-projet-groupe5

### 2. Créer l'image 
Dans le terminal de l'IDE

```bash
docker compose -f docker/docker-compose.yml up -d
