package fr.projetjee;

import fr.projetjee.dao.*;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.enums.Status;
import fr.projetjee.model.*;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class TestProjet {

    private Session session;

    public static void main(String[] args) {
        TestProjet test = new TestProjet();
        try {
            test.run();
        } finally {
            HibernateUtil.shutdown(); // Fermeture propre de Hibernate
        }
    }

    private void run() {
        session = HibernateUtil.getSessionFactory().openSession();

        try {
            System.out.println("\n========================================");
            System.out.println("🚀 DÉBUT DES TESTS - PROJET JEE");
            System.out.println("========================================\n");

            // ==========================
            // 1️⃣ CRÉATION UTILISATEURS
            // ==========================
            session.beginTransaction();
            System.out.println("👥 Création des utilisateurs...");

            User user1 = save(new User("EMP001", "Dupont", "Jean", "jean.dupont@entreprise.fr"));
            user1.setAddress("123 Rue de la Paix, 75002 Paris");
            user1.setPhone("0601020304");
            user1.setGrade(Grade.SENIOR);
            user1.setRole(Role.CHEF_PROJET);

            User user2 = save(new User("EMP002", "Martin", "Sophie", "sophie.martin@entreprise.fr"));
            user2.setPhone("0602030405");
            user2.setAddress("456 Avenue des Champs, 75008 Paris");
            user2.setGrade(Grade.JUNIOR);
            user2.setRole(Role.EMPLOYE);

            User user3 = save(new User("EMP003", "Bernard", "Luc", "luc.bernard@entreprise.fr"));
            user3.setPhone("0603040506");
            user3.setAddress("789 Boulevard Haussmann, 75009 Paris");
            user3.setGrade(Grade.EXPERT);
            user3.setRole(Role.CHEF_PROJET);


            User user4 = save(new User("EMP004", "Lefevre", "Marie", "marie.lefevre@entreprise.fr"));
            user4.setPhone("0604050607");
            user4.setAddress("321 Rue de Rivoli, 75004 Paris");
            user4.setGrade(Grade.SENIOR);
            user4.setRole(Role.CHEF_DEPARTEMENT);

            User user5 = save(new User("EMP005", "Moreau", "Pierre", "pierre.moreau@entreprise.fr"));
            user5.setPhone("0605060708");
            user5.setAddress("654 Avenue Victor Hugo, 75016 Paris");
            user5.setGrade(Grade.JUNIOR);
            user5.setRole(Role.EMPLOYE);

            User user6 = save(new User("EMP006", "Petit", "Claire", "claire.petit@entreprise.fr"));
            user6.setPhone("0606070809");
            user6.setAddress("987 Boulevard Saint-Michel, 75005 Paris");
            user6.setGrade(Grade.EXPERT);
            user6.setRole(Role.CHEF_DEPARTEMENT);

            session.getTransaction().commit();
            System.out.println("✅ 6 utilisateurs créés\n");

            // ==========================
            // 2️⃣ CRÉATION PROJETS
            // ==========================
            session.beginTransaction();
            System.out.println("📊 Création des projets...");

            Project project1 = save(new Project("Refonte Site Web", "Refonte complète du site web avec React et Spring Boot", Status.IN_PROGRESS));
            Project project2 = save(new Project("Application Mobile", "Développement d'une app mobile iOS et Android", Status.IN_PROGRESS));
            Project project3 = save(new Project("Migration Cloud", "Migration de l’infrastructure vers AWS", Status.COMPLETED));
            Project project4 = save(new Project("ERP Interne", "Mise en place d’un ERP interne", Status.CANCELLED));
            Project project5 = save(new Project("API Gateway", "Développement d’une API Gateway centralisée", Status.IN_PROGRESS));

            session.getTransaction().commit();
            System.out.println("✅ 5 projets créés\n");

            // ==========================
            // 3️⃣ CRÉATION FICHES DE PAIE
            // ==========================
            session.beginTransaction();
            System.out.println("💳 Création des fiches de paie...");

            Payslip payslip1 = save(new Payslip(2024, 10, new BigDecimal("4500.00"), new BigDecimal("500.00"), new BigDecimal("1000.00"), user1));
            Payslip payslip2 = save(new Payslip(2024, 10, new BigDecimal("2500.00"), new BigDecimal("200.00"), new BigDecimal("600.00"), user2));
            Payslip payslip3 = save(new Payslip(2024, 10, new BigDecimal("5500.00"), new BigDecimal("1000.00"), new BigDecimal("1400.00"), user3));
            Payslip payslip4 = save(new Payslip(2024, 10, new BigDecimal("4000.00"), new BigDecimal("300.00"), new BigDecimal("900.00"), user4));
            Payslip payslip5 = save(new Payslip(2024, 10, new BigDecimal("2300.00"), new BigDecimal("150.00"), new BigDecimal("550.00"), user5));

            session.getTransaction().commit();
            System.out.println("✅ 5 fiches de paie créées\n");

            // ==========================
            // 4️⃣ TESTS DAO UTILISATEUR
            // ==========================
            System.out.println("🔍 Tests DAO User...");
            UserDAO userDAO = new UserDAO();

            List<User> seniors = userDAO.findByGrade(Grade.SENIOR);
            System.out.println("Utilisateurs SENIOR : " + seniors.size());
            seniors.forEach(u -> System.out.println("   → " + u.getFullName()));

            // ==========================
            // 5️⃣ TESTS DAO PROJECT
            // ==========================
            System.out.println("\n🔍 Tests DAO Project...");
            ProjectDAO projectDAO = new ProjectDAO();

            // findByStatus
            List<Project> inProgress = projectDAO.findByStatus(Status.IN_PROGRESS);
            System.out.println("Projets EN COURS : " + inProgress.size());
            inProgress.forEach(p -> System.out.println("   → " + p.getName()));

            // findById / findByName
            Optional<Project> optProject = projectDAO.findById(project1.getId());
            optProject.ifPresent(p -> System.out.println("Projet trouvé par ID : " + p.getName()));

            Optional<Project> optByName = projectDAO.findByName("API Gateway");
            optByName.ifPresent(p -> System.out.println("Projet trouvé par nom : " + p.getName()));

            // assignUserToProject
            boolean assigned = projectDAO.assignUserToProject(project3.getId(), "EMP005");
            System.out.println("Assignation EMP005 au projet " + project3.getName() + ": " + (assigned ? "✅ OK" : "❌ Échec"));

            boolean assigned2 = projectDAO.assignUserToProject(project2.getId(), "EMP002");
            System.out.println("Assignation EMP002 au projet " + project2.getName() + ": " + (assigned2 ? "✅ OK" : "❌ Échec"));

            // updateStatus
            boolean statusUpdated = projectDAO.updateStatus(project2.getId(), Status.COMPLETED);
            System.out.println("Mise à jour status projet ID=" + project2.getId() + ": " + (statusUpdated ? "✅ OK" : "❌ Échec"));

            // findByUserId
            List<Project> projectsOfUser = projectDAO.findByUserId(user1.getId());
            System.out.println("Projets de " + user1.getFullName() + ": " + projectsOfUser.size());
            projectsOfUser.forEach(p -> System.out.println("   → " + p.getName()));

            // removeUserFromProject
            boolean removed = projectDAO.removeUserFromProject(project3.getId(), "EMP005");
            System.out.println("Retrait EMP005 du projet " + project3.getName() + ": " + (removed ? "✅ OK" : "❌ Échec"));

            // ==========================
            // 6️⃣ TESTS DAO PAYSLIP
            // ==========================
            System.out.println("\n🔍 Tests DAO Payslip...");
            PayslipDAO payslipDAO = new PayslipDAO();

            // findByUser
            List<Payslip> user1Payslips = payslipDAO.findByUser(user1);
            System.out.println("Fiches de paie de " + user1.getFullName() + ": " + user1Payslips.size());

            // findByMonth
            List<Payslip> octPayslips = payslipDAO.findByMonth(user1, 2024, 10);
            System.out.println("Fiches de paie d'octobre 2024 pour " + user1.getFullName() + ": " + octPayslips.size());

            // update
            Payslip p = octPayslips.get(0);
            p.setNetPay(new BigDecimal("4800.00"));
            payslipDAO.update(p);
            System.out.println("✅ Fiche de paie mise à jour, nouveau net: " + p.getNetPay());

            // findById
            Optional<Payslip> optPayslip = payslipDAO.findById(p.getId());
            optPayslip.ifPresent(pay -> System.out.println("Fiche de paie trouvée par ID: " + pay.getNetPay()));

            // delete
            boolean deleted = payslipDAO.delete(p.getId());
            System.out.println("Suppression fiche de paie ID=" + p.getId() + ": " + (deleted ? "✅ OK" : "❌ Échec"));

            System.out.println("\n✅ TOUS LES TESTS TERMINÉS AVEC SUCCÈS!");

        } catch (Exception e) {
            e.printStackTrace();
            if (session.getTransaction().isActive()) session.getTransaction().rollback();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    private <T> T save(T entity) {
        session.persist(entity);
        return entity;
    }
}
