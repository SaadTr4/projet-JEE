package fr.projetjee;

import fr.projetjee.dao.*;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.enums.Status;
import fr.projetjee.model.*;
import fr.projetjee.util.HibernateUtil;

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
            Department informatique = departmentDAO.save(new Department("Informatique", "Gestion du SI et du développement logiciel"));
            Department rh = departmentDAO.save(new Department("Ressources Humaines", "Recrutement et gestion du personnel"));
            Department finance = departmentDAO.save(new Department("Finance", "Comptabilité et gestion financière"));

            Position devBackend = positionDAO.save(new Position("Développeur Backend", "Développement des services backend"));
            Position devFrontend = positionDAO.save(new Position("Développeur Frontend", "Développement des interfaces utilisateurs"));
            Position chefProjet = positionDAO.save(new Position("Chef de Projet", "Pilotage et coordination de projet"));
            Position chefDepartement = positionDAO.save(new Position("Chef de Département", "Responsable d’un département"));

            System.out.println("✅ Départements et postes créés.\n");

            // ==========================
            // UTILISATEURS
            // ==========================
            User jean = new User("EMP001", "Dupont", "Jean", "jean.dupont@entreprise.fr");
            jean.setGrade(Grade.SENIOR);
            jean.setRole(Role.CHEF_PROJET);
            jean.setDepartment(informatique);
            jean.setPosition(chefProjet);
            userDAO.save(jean);

            User sophie = new User("EMP002", "Martin", "Sophie", "sophie.martin@entreprise.fr");
            sophie.setGrade(Grade.JUNIOR);
            sophie.setRole(Role.EMPLOYE);
            sophie.setDepartment(informatique);
            sophie.setPosition(devBackend);
            userDAO.save(sophie);

            User luc = new User("EMP003", "Bernard", "Luc", "luc.bernard@entreprise.fr");
            luc.setGrade(Grade.EXPERT);
            luc.setRole(Role.CHEF_DEPARTEMENT);
            luc.setDepartment(rh);
            luc.setPosition(chefDepartement);
            userDAO.save(luc);

            User marie = new User("EMP004", "Lefevre", "Marie", "marie.lefevre@entreprise.fr");
            marie.setGrade(Grade.SENIOR);
            marie.setRole(Role.CHEF_PROJET);
            marie.setDepartment(finance);
            marie.setPosition(chefProjet);
            userDAO.save(marie);

            System.out.println("✅ Utilisateurs créés et assignés.\n");

            // ==========================
            // PROJETS
            // ==========================
            Project siteWeb = new Project("Refonte Site Web", jean, "Refonte complète du site de l’entreprise", Status.IN_PROGRESS);
            Project mobileApp = new Project("Application Mobile", marie, "Développement d’une application mobile interne", Status.PLANNED);
            Project cloudMigration = new Project("Migration Cloud", luc, "Migration des serveurs internes vers AWS", Status.IN_PROGRESS);

            projectDAO.save(siteWeb);
            projectDAO.save(mobileApp);
            projectDAO.save(cloudMigration);

            // Assignation d’utilisateurs
            projectDAO.assignUserToProject(siteWeb.getId(), "EMP002");
            projectDAO.assignUserToProject(cloudMigration.getId(), "EMP001");

            System.out.println("✅ Projets créés et utilisateurs assignés.\n");

            // ==========================
            // FICHES DE PAIE
            // ==========================
            payslipDAO.save(new Payslip(2023, 11, new BigDecimal("4500.00"), new BigDecimal("500.00"), new BigDecimal("1000.00"), jean));
            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("2500.00"), new BigDecimal("200.00"), new BigDecimal("600.00"), sophie));
            payslipDAO.save(new Payslip(2024, 10, new BigDecimal("5500.00"), new BigDecimal("1000.00"), new BigDecimal("1400.00"), luc));

            System.out.println("✅ Fiches de paie créées.\n");

            System.out.println("🎉 Base de données initialisée avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de l’initialisation de la base de données.");
        }
    }
}
