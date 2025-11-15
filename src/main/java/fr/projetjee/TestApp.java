package fr.projetjee;

import fr.projetjee.dao.*;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.enums.Status;
import fr.projetjee.model.*;
import fr.projetjee.util.HibernateUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class TestApp {

    public static void main(String[] args) {
        TestApp test = new TestApp();
        try {
            test.run();
        } finally {
            HibernateUtil.shutdown(); // Fermeture propre de Hibernate
        }
    }

    private void run() {
        try {
            System.out.println("\n========================================");
            System.out.println("🚀 DÉBUT DES TESTS - PROJET JEE");
            System.out.println("========================================\n");

            // ==========================
            // DAO
            // ==========================
            UserDAO userDAO = new UserDAO();
            ProjectDAO projectDAO = new ProjectDAO();
            PayslipDAO payslipDAO = new PayslipDAO();
            DepartmentDAO departmentDAO = new DepartmentDAO();
            PositionDAO positionDAO = new PositionDAO();

            // ==========================
            // 1️⃣ CRÉATION UTILISATEURS
            // ==========================
            System.out.println("👥 Création des utilisateurs...");

            User user1 = userDAO.save(new User("EMP001_TEST", "DupontTest", "Jean", "jean.dupontTest@entreprise.fr"));
            user1.setAddress("123 Rue de la Paix, 75002 Paris");
            user1.setPhone("0601020304");
            user1.setGrade(Grade.SENIOR);
            user1.setRole(Role.CHEF_PROJET);
            userDAO.update(user1);

            User user2 = userDAO.save(new User("EMP002_TEST", "MartinTest", "Sophie", "sophie.martinTest@entreprise.fr"));
            user2.setAddress("456 Avenue des Champs, 75008 Paris");
            user2.setPhone("0602030405");
            user2.setGrade(Grade.JUNIOR);
            user2.setRole(Role.EMPLOYE);
            userDAO.update(user2);

            User user3 = userDAO.save(new User("EMP003_TEST", "BernardTest", "Luc", "luc.bernardTest@entreprise.fr"));
            user3.setAddress("789 Boulevard Haussmann, 75009 Paris");
            user3.setPhone("0603040506");
            user3.setGrade(Grade.EXPERT);
            user3.setRole(Role.CHEF_PROJET);
            userDAO.update(user3);

            User user4 = userDAO.save(new User("EMP004_TEST", "LefevreTest", "Marie", "marie.lefevreTest@entreprise.fr"));
            user4.setAddress("321 Rue de Rivoli, 75004 Paris");
            user4.setPhone("0604050607");
            user4.setGrade(Grade.SENIOR);
            user4.setRole(Role.CHEF_DEPARTEMENT);
            userDAO.update(user4);

            User user5 = userDAO.save(new User("EMP005_TEST", "MoreauTest", "Pierre", "pierre.moreauTest@entreprise.fr"));
            user5.setAddress("654 Avenue Victor Hugo, 75016 Paris");
            user5.setPhone("0605060708");
            user5.setGrade(Grade.JUNIOR);
            user5.setRole(Role.EMPLOYE);
            userDAO.update(user5);

            User user6 = userDAO.save(new User("EMP006_TEST", "PetitTest", "Claire", "claire.petitTest@entreprise.fr"));
            user6.setAddress("987 Boulevard Saint-Michel, 75005 Paris");
            user6.setPhone("0606070809");
            user6.setGrade(Grade.EXPERT);
            user6.setRole(Role.CHEF_DEPARTEMENT);
            userDAO.update(user6);

            System.out.println("✅ 6 utilisateurs créés\n");

            // ==========================
            // 2️⃣ CRÉATION PROJETS
            // ==========================
            System.out.println("📊 Création des projets...");

            Project project1 = projectDAO.save(new Project("[Test] Refonte Site Web", user1, "Refonte complète du site web avec React et Spring Boot", Status.IN_PROGRESS));
            Project project2 = projectDAO.save(new Project("[Test] Application Mobile", user2, "Développement d'une app mobile iOS et Android", Status.IN_PROGRESS));
            Project project3 = projectDAO.save(new Project("[Test] Migration Cloud", user3, "Migration de l’infrastructure vers AWS", Status.COMPLETED));
            Project project4 = projectDAO.save(new Project("[Test] ERP Interne", user4, "Mise en place d’un ERP interne", Status.CANCELLED));
            Project project5 = projectDAO.save(new Project("[Test] API Gateway", user1, "Développement d’une API Gateway centralisée", Status.IN_PROGRESS));

            System.out.println("✅ 5 projets créés\n");

            // ==========================
            // 3️⃣ CRÉATION FICHES DE PAIE
            // ==========================
            System.out.println("💳 Création des fiches de paie...");

            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("4500.00"), new BigDecimal("500.00"), new BigDecimal("1000.00"), user1));
            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("2500.00"), new BigDecimal("200.00"), new BigDecimal("600.00"), user2));
            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("5500.00"), new BigDecimal("1000.00"), new BigDecimal("1400.00"), user3));
            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("4000.00"), new BigDecimal("300.00"), new BigDecimal("900.00"), user4));
            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("2300.00"), new BigDecimal("150.00"), new BigDecimal("550.00"), user5));

            System.out.println("✅ 5 fiches de paie créées\n");

            // ==========================
            // CRÉATION DÉPARTEMENTS & POSTES
            System.out.println("🏢 Création des départements et postes...");
            Department dep1 = departmentDAO.save(new Department("[Test] Informatique", "IT","Gestion des systèmes d'information"));
            Department dep2 = departmentDAO.save(new Department("[Test] Ressources Humaines", "RH","Gestion du personnel et des recrutements"));
            Position pos1 = positionDAO.save(new Position("[Test] Développeur Backend", "Développement des services backend"));
            Position pos2 = positionDAO.save(new Position("[Test] Chef de Projet", "Gestion et coordination des projets"));

            System.out.println("✅ Départements et postes créés\n");



            // ==========================
            // 5️⃣ TESTS DAO PROJECT
            // ==========================
            System.out.println("\n🔍 Tests DAO Project...");

            List<Project> inProgress = projectDAO.findByStatus(Status.IN_PROGRESS);
            System.out.println("Projets EN COURS : " + inProgress.size());
            inProgress.forEach(p -> System.out.println("   → " + p.getName()));

            // assignUserToProject
            boolean assigned1 = projectDAO.assignUserToProject(project3.getId(), "EMP005_TEST");
            boolean assigned2 = projectDAO.assignUserToProject(project2.getId(), "EMP002_TEST");
            boolean assigned3 = projectDAO.assignUserToProject(project1.getId(), "EMP001_TEST");

            System.out.println("Assignation EMP005_TEST au projet " + project3.getName() + ": " + (assigned1 ? "✅ OK" : "❌ Échec"));
            System.out.println("Assignation EMP002_TEST au projet " + project2.getName() + ": " + (assigned2 ? "✅ OK" : "❌ Échec"));
            System.out.println("Assignation EMP001_TEST au projet " + project1.getName() + ": " + (assigned3 ? "✅ OK" : "❌ Échec"));

            // updateStatus
            boolean statusUpdated = projectDAO.updateStatus(project2.getId(), Status.COMPLETED);
            System.out.println("Mise à jour status projet ID=" + project2.getId() + ": " + (statusUpdated ? "✅ OK" : "❌ Échec"));

            // findByUserId
            List<Project> projectsOfUser1 = projectDAO.findByUserId(user1.getId());
            System.out.println("Projets de " + user1.getFullName() + ": " + projectsOfUser1.size());
            projectsOfUser1.forEach(p -> System.out.println("   → " + p.getName()));

            // removeUserFromProject
            boolean removed = projectDAO.removeUserFromProject(project3.getId(), "EMP005_TEST");
            System.out.println("Retrait EMP005_TEST du projet " + project3.getName() + ": " + (removed ? "✅ OK" : "❌ Échec"));

            // findAll
            List<Project> allProjects = projectDAO.findAll();
            System.out.println("Nombre total de projets (findAll): " + allProjects.size());

            // findByName
            Optional<Project> projectsByName = projectDAO.findByName("Application Mobile");
            projectsByName.ifPresentOrElse(
                    p -> System.out.println("Projet trouvé par nom: " + p.getName()),
                    () -> System.out.println("❌ Aucun projet trouvé avec ce nom.")
            );


            // ==========================
            // 6️⃣ TESTS DAO PAYSLIP
            // ==========================
            System.out.println("\n🔍 Tests DAO Payslip...");
            List<Payslip> user1Payslips = payslipDAO.findByUser(user1);
            System.out.println("Fiches de paie de " + user1.getFullName() + ": " + user1Payslips.size());

            Optional<Payslip> optPayslip = payslipDAO.findById(user1Payslips.get(0).getId());
            optPayslip.ifPresent(p -> {
                p.setNetPay(new BigDecimal("4800.00"));
                payslipDAO.update(p);
                System.out.println("✅ Fiche de paie mise à jour, nouveau net: " + p.getNetPay());
            });
            // findAll
            List<Payslip> allPayslips = payslipDAO.findAll();
            System.out.println("Nombre total de fiches de paie (findAll): " + allPayslips.size());

            // findByMonth
            List<Payslip> octoberPayslips = payslipDAO.findByMonth(user2, 2024, 10);
            System.out.println("Fiches de paie d'octobre 2024 pour " + user2.getFullName() + ": " + octoberPayslips.size());


            // ==========================
            // TEST DAO DÉPARTEMENT & POSTE
            // ========================

            user1.setDepartment(dep1);
            user1.setPosition(pos1);
            user2.setDepartment(dep1);
            user2.setPosition(pos2);
            System.out.println("Departements et postes assignés aux utilisateurs avec set.\n");

            departmentDAO.assignUserToDepartment(dep2.getId(), user3.getMatricule());
            positionDAO.assignUserToPosition(pos2.getId(), user3.getMatricule());
            System.out.println("Departements et postes assignés aux utilisateurs avec DAO.\n");

            // findUsersByDepartment
            List<User> usersInDep1 = departmentDAO.findUsersByDepartment(dep1.getId());
            System.out.println("Utilisateurs dans le département " + dep1.getName() + ": " + usersInDep1.size());

            // countUsersByDepartment
            long countUsersDep2 = departmentDAO.countUsersByDepartment(dep2.getId());
            System.out.println("Nombre d'utilisateurs dans le département " + dep2.getName() + ": " + countUsersDep2);

            // findUsersByPosition
            List<User> usersInPos2 = positionDAO.findUsersByPosition(pos2.getId());
            System.out.println("Utilisateurs dans le poste " + pos2.getName() + ": " + usersInPos2.size());

            // countUsersByPosition
            long countUsersPos1 = positionDAO.countUsersByPosition(pos1.getId());
            System.out.println("Nombre d'utilisateurs dans le poste " + pos1.getName() + ": " + countUsersPos1);

            // ==========================
            // 4️⃣ TESTS DAO UTILISATEUR
            // ==========================
            System.out.println("🔍 Tests DAO User...");
            List<User> seniors = userDAO.findByGrade(Grade.SENIOR);
            System.out.println("Utilisateurs SENIOR : " + seniors.size());
            seniors.forEach(u -> System.out.println("   → " + u.getFullName()));

            // findByProject
            List<User> usersInProject1 = userDAO.findByProject(project1.getId());
            System.out.println("Utilisateurs dans le projet " + project1.getName() + ": " + usersInProject1.size());

            // findByDepartment
            List<User> usersInDepartment1 = userDAO.findByDepartment(dep1.getId());
            System.out.println("Utilisateurs dans le département " + dep1.getName() + ": " + usersInDepartment1.size());

            // findByPosition
            List<User> usersInPosition1 = userDAO.findByPosition(pos1.getId());
            System.out.println("Utilisateurs dans le poste " + pos1.getName() + ": " + usersInPosition1.size());

            // countByDepartment
            long countByDepartment1 = userDAO.countByDepartment(dep1.getId());
            System.out.println("Nombre d'utilisateurs dans le département " + dep1.getName() + ": " + countByDepartment1);

            // isUserProjectManager
            boolean isManager = userDAO.isUserProjectManager(Role.CHEF_PROJET, user1.getId());
            System.out.println(user1.getFullName() + " est-il chef de projet pour " + project1.getName() + "? " + (isManager ? "✅ Oui" : "❌ Non"));

            System.out.println("\n✅ TESTS DAO TERMINÉS\n");
            // ==========================
            // 🧹 SUPPRESSION DES DONNÉES DE TEST
            // ==========================
            System.out.println("\n🧹 Suppression des données de test...");

            // ⚠️ Important : respecter l’ordre pour éviter les erreurs de contraintes FK

            // 1️⃣ Supprimer les fiches de paie
            payslipDAO.findAll().stream()
                    .filter(p -> p.getUser() != null && p.getUser().getMatricule().endsWith("_TEST"))
                    .forEach(p -> {
                        payslipDAO.delete(p);
                        System.out.println("   → Fiche de paie supprimée : ID=" + p.getId());
                    });

            // 2️⃣ Supprimer les projets de test
            projectDAO.findAll().stream()
                    .filter(p -> p.getName().startsWith("[Test]"))
                    .forEach(p -> {
                        projectDAO.delete(p);
                        System.out.println("   → Projet supprimé : " + p.getName());
                    });

            // 3️⃣ Supprimer les utilisateurs de test
            userDAO.findAll().stream()
                    .filter(u -> u.getMatricule() != null && u.getMatricule().endsWith("_TEST"))
                    .forEach(u -> {
                        userDAO.delete(u);
                        System.out.println("   → Utilisateur supprimé : " + u.getFullName());
                    });

            // 4️⃣ Supprimer les départements et postes de test
            departmentDAO.findAll().stream()
                    .filter(d -> d.getName().startsWith("[Test]"))
                    .forEach(d -> {
                        departmentDAO.delete(d);
                        System.out.println("   → Département supprimé : " + d.getName());
                    });

            positionDAO.findAll().stream()
                    .filter(p -> p.getName().startsWith("[Test]"))
                    .forEach(p -> {
                        positionDAO.delete(p);
                        System.out.println("   → Poste supprimé : " + p.getName());
                    });

            System.out.println("✅ Données de test supprimées proprement.\n");

            System.out.println("\n✅ TOUS LES TESTS TERMINÉS AVEC SUCCÈS!");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
