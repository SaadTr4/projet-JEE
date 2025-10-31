package fr.projetjee;


import fr.projetjee.model.*;
import fr.projetjee.dao.*;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.enums.Status;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.List;

public class TestProjet {

    private Session session;

    public static void main(String[] args) {
        TestProjet test = new TestProjet();
        try {
            test.run();
        } finally {
            HibernateUtil.shutdown(); // ✅ Fermeture propre
        }
    }

    private void run() {
        // Ouverture d'une session Hibernate
        session = HibernateUtil.getSessionFactory().openSession();

        try {
            session.beginTransaction();

            System.out.println("\n========================================");
            System.out.println("🚀 DÉBUT DES TESTS - PROJET JEE");
            System.out.println("========================================\n");

            // --- 1️⃣ CRÉATION UTILISATEURS ---
            System.out.println("👥 CRÉATION DES UTILISATEURS...");
            User user1 = new User("EMP001", "Dupont", "Jean", "jean.dupont@entreprise.fr");
            user1.setPhone("0601020304");
            user1.setAddress("123 Rue de Paris, 75001 Paris");
            user1.setGrade(Grade.SENIOR);
            user1.setRole(Role.CHEF_PROJET);
            save(user1);

            User user2 = new User("EMP002", "Martin", "Sophie", "sophie.martin@entreprise.fr");
            user2.setPhone("0602030405");
            user2.setAddress("456 Avenue des Champs, 75008 Paris");
            user2.setGrade(Grade.JUNIOR);
            user2.setRole(Role.EMPLOYE);
            save(user2);

            User user3 = new User("EMP003", "Bernard", "Luc", "luc.bernard@entreprise.fr");
            user3.setPhone("0603040506");
            user3.setAddress("789 Boulevard Haussmann, 75009 Paris");
            user3.setGrade(Grade.EXPERT);
            user3.setRole(Role.ADMINISTRATEUR);
            save(user3);

            User user4 = new User("EMP004", "Lefevre", "Marie", "marie.lefevre@entreprise.fr");
            user4.setPhone("0604050607");
            user4.setAddress("321 Rue de Rivoli, 75004 Paris");
            user4.setGrade(Grade.SENIOR);
            user4.setRole(Role.CHEF_DEPARTEMENT);
            save(user4);

            User user5 = new User("EMP005", "Moreau", "Pierre", "pierre.moreau@entreprise.fr");
            user5.setPhone("0605060708");
            user5.setAddress("654 Avenue Montaigne, 75008 Paris");
            user5.setGrade(Grade.JUNIOR);
            user5.setRole(Role.EMPLOYE);
            save(user5);

            User user6 = new User("EMP006", "Petit", "Claire", "claire.petit@entreprise.fr");
            user6.setPhone("0606070809");
            user6.setAddress("987 Rue du Faubourg, 75010 Paris");
            user6.setGrade(Grade.EXPERT);
            user6.setRole(Role.CHEF_PROJET);
            save(user6);

            session.getTransaction().commit();
            System.out.println("✅ 6 utilisateurs créés\n");

            // ==========================================
            // NOUVELLE TRANSACTION pour la suite
            // ==========================================
            session.beginTransaction();

            System.out.println("📊 CRÉATION DES PROJETS...");
            Project project1 = save(new Project("Refonte Site Web",
                    "Refonte complète du site web avec React et Spring Boot", Status.IN_PROGRESS));
            Project project2 = save(new Project("Application Mobile",
                    "Développement d'une app mobile iOS et Android", Status.IN_PROGRESS));
            Project project3 = save(new Project("Migration Cloud",
                    "Migration de l’infrastructure vers AWS", Status.COMPLETED));
            Project project4 = save(new Project("ERP Interne",
                    "Mise en place d’un ERP interne", Status.CANCELLED));
            Project project5 = save(new Project("API Gateway",
                    "Développement d’une API Gateway centralisée", Status.IN_PROGRESS));
            session.getTransaction().commit();
            System.out.println("✅ 5 projets créés\n");

            // ==========================================
            // 3️⃣ ASSOCIATION UTILISATEURS ↔ PROJETS
            // ==========================================
            session.beginTransaction();
            user1.getProjects().add(project1);
            user1.getProjects().add(project2);
            project1.getUsers().add(user1);
            project2.getUsers().add(user1);

            user2.getProjects().add(project1);
            project1.getUsers().add(user2);

            user3.getProjects().add(project3);
            user3.getProjects().add(project5);
            project3.getUsers().add(user3);
            project5.getUsers().add(user3);

            user4.getProjects().add(project3);
            project3.getUsers().add(user4);

            user5.getProjects().add(project2);
            project2.getUsers().add(user5);

            user6.getProjects().add(project5);
            user6.getProjects().add(project1);
            project5.getUsers().add(user6);
            project1.getUsers().add(user6);

            session.merge(user1);
            session.merge(user2);
            session.merge(user3);
            session.merge(user4);
            session.merge(user5);
            session.merge(user6);
            session.getTransaction().commit();

            System.out.println("✅ Associations créées\n");

            // ==========================================
            // 4️⃣ FICHES DE PAIE
            // ==========================================
            session.beginTransaction();
            save(new Payslip(2024, 10, new BigDecimal("4500.00"), new BigDecimal("500.00"), new BigDecimal("1000.00"), user1));
            save(new Payslip(2024, 10, new BigDecimal("2500.00"), new BigDecimal("200.00"), new BigDecimal("600.00"), user2));
            save(new Payslip(2024, 10, new BigDecimal("5500.00"), new BigDecimal("1000.00"), new BigDecimal("1400.00"), user3));
            save(new Payslip(2024, 10, new BigDecimal("4000.00"), new BigDecimal("300.00"), new BigDecimal("900.00"), user4));
            save(new Payslip(2024, 10, new BigDecimal("2300.00"), new BigDecimal("150.00"), new BigDecimal("550.00"), user5));
            session.getTransaction().commit();
            System.out.println("✅ Fiches de paie créées\n");

            // ==========================================
            // 5️⃣ TESTS DES DAO
            // ==========================================
            System.out.println("🔍 TESTS AVEC DAO...\n");
            UserDAO userDAO = new UserDAO();
            ProjectDAO projectDAO = new ProjectDAO();
            PayslipDAO payslipDAO = new PayslipDAO();

            List<User> seniors = userDAO.findByGrade(Grade.SENIOR);
            System.out.println("👨‍💼 Utilisateurs SENIOR : " + seniors.size());
            seniors.forEach(u -> System.out.println("   → " + u.getFullName()));

            List<Project> inProgress = projectDAO.findByStatus(Status.IN_PROGRESS);
            System.out.println("\n📊 Projets EN COURS : " + inProgress.size());
            inProgress.forEach(p -> System.out.println("   → " + p.getName()));

            List<Payslip> payslips = payslipDAO.findByUser(user1);
            System.out.println("\n💰 Fiches de paie de " + user1.getFullName() + " : " + payslips.size());
            payslips.forEach(p -> System.out.println("   → " + p.getMonth() + "/" + p.getYear() + " - Net: " + p.getNetPay()));

            System.out.println("\n✅ TESTS TERMINÉS AVEC SUCCÈS!");

        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private <T> T save(T entity) {
        session.persist(entity);
        return entity;
    }
}
