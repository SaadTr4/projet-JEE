package fr.projetjee;

import fr.projetjee.dao.*;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.enums.Status;
import fr.projetjee.model.*;
import fr.projetjee.util.HibernateUtil;
import fr.projetjee.util.PasswordUtil;

import java.math.BigDecimal;

public class DataInitializer {

    public static void main(String[] args) {
        DataInitializer init = new DataInitializer();
        try {
            init.initializeDatabase();
        } finally {
            HibernateUtil.shutdown();
        }
    }

    public void initializeDatabase() {
        System.out.println("\n========================================");
        System.out.println("🚀 INITIALISATION DE LA BASE DE DONNÉES");
        System.out.println("========================================\n");

        // ==========================
        // VIDER LES TABLES ET RESET DES IDS
        // ==========================
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.doWork(connection -> {
                try (var stmt = connection.createStatement()) {
                    stmt.executeUpdate("TRUNCATE TABLE payslip RESTART IDENTITY CASCADE");
                    stmt.executeUpdate("TRUNCATE TABLE user_project RESTART IDENTITY CASCADE");
                    stmt.executeUpdate("TRUNCATE TABLE project RESTART IDENTITY CASCADE");
                    stmt.executeUpdate("TRUNCATE TABLE user_account RESTART IDENTITY CASCADE");
                    stmt.executeUpdate("TRUNCATE TABLE position RESTART IDENTITY CASCADE");
                    stmt.executeUpdate("TRUNCATE TABLE department RESTART IDENTITY CASCADE");
                }
            });
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors du vidage des tables.");
            return;
        }
        System.out.println("✅ Tables vidées et séquences réinitialisées.\n");

        try {
            // ==========================
            // DAO
            // ==========================
            UserDAO userDAO = new UserDAO();
            ProjectDAO projectDAO = new ProjectDAO();
            PayslipDAO payslipDAO = new PayslipDAO();
            DepartmentDAO departmentDAO = new DepartmentDAO();
            PositionDAO positionDAO = new PositionDAO();

            // ==========================
            // DÉPARTEMENTS & POSTES
            // ==========================
            Department informatique = departmentDAO.save(new Department("Informatique","IT", "Gestion du SI et du développement logiciel"));
            Department rh = departmentDAO.save(new Department("Ressources Humaines","RH", "Recrutement et gestion du personnel"));
            Department finance = departmentDAO.save(new Department("Finance", "FIN","Comptabilité et gestion financière"));

            Position devBackend = positionDAO.save(new Position("Développeur Backend", "Développement des services backend"));
            Position devFrontend = positionDAO.save(new Position("Développeur Frontend", "Développement des interfaces utilisateurs"));
            Position chefProjet = positionDAO.save(new Position("Chef de Projet", "Pilotage et coordination de projet"));
            Position chefDepartement = positionDAO.save(new Position("Chef de Département", "Responsable d’un département"));
            Position administrateurSysteme = positionDAO.save(new Position("Administrateur Système", "Gestion des infrastructures IT"));

            System.out.println("✅ Départements et postes créés.\n");

            // ==========================
            // UTILISATEURS
            // ==========================
            User jean_claude = new User("EMP001", "Ilboudo", "Jean-Claude", "jean_claude.ilboudo@entreprise.fr");
            jean_claude.setGrade(Grade.SENIOR);
            jean_claude.setRole(Role.CHEF_PROJET);
            jean_claude.setDepartment(informatique);
            jean_claude.setPosition(chefProjet);
            jean_claude.setPassword(PasswordUtil.hashPassword("motdepasse123"));
            userDAO.save(jean_claude);

            User saad = new User("EMP002", "Tarmidi", "Saad", "saad.tarmidi@entreprise.fr");
            saad.setGrade(Grade.JUNIOR);
            saad.setRole(Role.EMPLOYE);
            saad.setDepartment(rh);
            saad.setPosition(devBackend);
            saad.setPassword(PasswordUtil.hashPassword("motdepasse123"));
            userDAO.save(saad);

            User adam = new User("EMP003", "Swiczka", "Adam", "adam.swiczka@entreprise.fr");
            adam.setGrade(Grade.EXPERT);
            adam.setRole(Role.CHEF_DEPARTEMENT);
            adam.setDepartment(finance);
            adam.setPosition(chefDepartement);
            adam.setPassword(PasswordUtil.hashPassword("motdepasse123"));
            userDAO.save(adam);

            User haitam = new User("EMP004", "Hania", "Haitam", "haitam.hania@entreprise.fr");
            haitam.setGrade(Grade.SENIOR);
            haitam.setRole(Role.ADMINISTRATEUR);
            haitam.setDepartment(informatique);
            haitam.setPosition(administrateurSysteme);
            haitam.setPassword(PasswordUtil.hashPassword("motdepasse123"));
            userDAO.save(haitam);

            User medhi = new User("EMP005","nom","Medhi","medhi.nom@entreprise.fr");
            medhi.setGrade(Grade.JUNIOR);
            medhi.setRole(Role.EMPLOYE);
            medhi.setDepartment(finance);
            medhi.setPosition(devFrontend);
            medhi.setPassword(PasswordUtil.hashPassword("motdepasse123"));
            userDAO.save(medhi);

            System.out.println("✅ Utilisateurs créés et assignés.\n");

            // ==========================
            // PROJETS
            // ==========================
            Project siteWeb = new Project("Refonte Site Web", jean_claude, "Refonte complète du site de l’entreprise", Status.IN_PROGRESS);
            Project mobileApp = new Project("Application Mobile", haitam, "Développement d’une application mobile interne", Status.PLANNED);
            Project cloudMigration = new Project("Migration Cloud", jean_claude, "Migration des serveurs internes vers AWS", Status.IN_PROGRESS);

            projectDAO.save(siteWeb);
            projectDAO.save(mobileApp);
            projectDAO.save(cloudMigration);

            // Assignation d’utilisateurs
            projectDAO.assignUserToProject(siteWeb.getId(), "EMP002");
            projectDAO.assignUserToProject(cloudMigration.getId(), "EMP001");
            projectDAO.assignUserToProject(siteWeb.getId(), "EMP001");
            projectDAO.assignUserToProject(mobileApp.getId(), "EMP004");
            projectDAO.assignUserToProject(cloudMigration.getId(), "EMP005");
            projectDAO.assignUserToProject(mobileApp.getId(), "EMP005");


            System.out.println("✅ Projets créés et utilisateurs assignés.\n");

            // ==========================
            // FICHES DE PAIE
            // ==========================
            payslipDAO.save(new Payslip(2023, 11, new BigDecimal("4500.00"), new BigDecimal("500.00"), new BigDecimal("1000.00"), jean_claude));
            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("2500.00"), new BigDecimal("200.00"), new BigDecimal("600.00"), saad));
            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("5500.00"), new BigDecimal("1000.00"), new BigDecimal("1400.00"), adam));
            payslipDAO.save(new Payslip(2023, 12, new BigDecimal("4000.00"), new BigDecimal("300.00"), new BigDecimal("800.00"), haitam));
            payslipDAO.save(new Payslip(2024, 9, new BigDecimal("2700.00"), new BigDecimal("250.00"), new BigDecimal("650.00"), medhi));
            payslipDAO.save(new Payslip(2024, 8, new BigDecimal("2500.00"), new BigDecimal("150.00"), new BigDecimal("350.00"), medhi));


            System.out.println("✅ Fiches de paie créées.\n");

            System.out.println("🎉 Base de données initialisée avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de l’initialisation de la base de données.");
        }
    }
}
