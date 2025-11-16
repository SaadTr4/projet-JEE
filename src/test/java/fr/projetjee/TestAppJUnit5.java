package fr.projetjee;

import fr.projetjee.dao.*;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.enums.Status;
import fr.projetjee.model.*;
import fr.projetjee.util.HibernateUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestAppJUnit5 {

    private UserDAO userDAO;
    private ProjectDAO projectDAO;
    private PayslipDAO payslipDAO;
    private DepartmentDAO departmentDAO;
    private PositionDAO positionDAO;

    private User user1, user2, user3, user4, user5, user6;
    private Project project1, project2, project3, project4, project5;
    private Department dep1, dep2;
    private Position pos1, pos2;

    @BeforeAll
    void initDAO() {
        userDAO = new UserDAO();
        projectDAO = new ProjectDAO();
        payslipDAO = new PayslipDAO();
        departmentDAO = new DepartmentDAO();
        positionDAO = new PositionDAO();

        // ==== DÉPARTEMENTS & POSTES ====
        dep1 = new Department("[Test] Informatique", "IT_TEST", "Gestion SI");
        dep2 = new Department("[Test] Ressources Humaines", "RH_TEST", "Gestion RH");
        pos1 = new Position("[Test] Développeur Backend", "Développement backend");
        pos2 = new Position("[Test] Chef de Projet", "Gestion projets");

        departmentDAO.save(dep1);
        departmentDAO.save(dep2);
        positionDAO.save(pos1);
        positionDAO.save(pos2);

        // ==== UTILISATEURS ====
        user1 = new User("EMP001_TEST", "DupontTest", "Jean", "jean.dupontTest@entreprise.fr");
        user1.setPassword("test123");
        user1.setAddress("123 Rue de la Paix, 75002 Paris");
        user1.setPhone("0601020304");
        user1.setGrade(Grade.SENIOR);
        user1.setRole(Role.CHEF_PROJET);
        userDAO.save(user1);

        user2 = new User("EMP002_TEST", "MartinTest", "Sophie", "sophie.martinTest@entreprise.fr");
        user2.setPassword("test123");
        user2.setAddress("456 Avenue des Champs, 75008 Paris");
        user2.setPhone("0602030405");
        user2.setGrade(Grade.JUNIOR);
        user2.setRole(Role.EMPLOYE);
        userDAO.save(user2);

        user3 = new User("EMP003_TEST", "BernardTest", "Luc", "luc.bernardTest@entreprise.fr");
        user3.setPassword("test123");
        user3.setAddress("789 Boulevard Haussmann, 75009 Paris");
        user3.setPhone("0603040506");
        user3.setGrade(Grade.EXPERT);
        user3.setRole(Role.CHEF_PROJET);
        userDAO.save(user3);

        user4 = new User("EMP004_TEST", "LefevreTest", "Marie", "marie.lefevreTest@entreprise.fr");
        user4.setPassword("test123");
        user4.setAddress("321 Rue de Rivoli, 75004 Paris");
        user4.setPhone("0604050607");
        user4.setGrade(Grade.SENIOR);
        user4.setRole(Role.CHEF_DEPARTEMENT);
        userDAO.save(user4);

        user5 = new User("EMP005_TEST", "MoreauTest", "Pierre", "pierre.moreauTest@entreprise.fr");
        user5.setPassword("test123");
        user5.setAddress("654 Avenue Victor Hugo, 75016 Paris");
        user5.setPhone("0605060708");
        user5.setGrade(Grade.JUNIOR);
        user5.setRole(Role.EMPLOYE);
        userDAO.save(user5);

        user6 = new User("EMP006_TEST", "PetitTest", "Claire", "claire.petitTest@entreprise.fr");
        user6.setPassword("test123");
        user6.setAddress("987 Boulevard Saint-Michel, 75005 Paris");
        user6.setPhone("0606070809");
        user6.setGrade(Grade.EXPERT);
        user6.setRole(Role.CHEF_DEPARTEMENT);
        userDAO.save(user6);

        // ==== PROJETS ====
        project1 = new Project("[Test] Refonte Site Web", user1, "Refonte complète du site web", Status.IN_PROGRESS);
        project2 = new Project("[Test] Application Mobile", user2, "Développement app mobile", Status.IN_PROGRESS);
        project3 = new Project("[Test] Migration Cloud", user3, "Migration vers AWS", Status.COMPLETED);
        project4 = new Project("[Test] ERP Interne", user4, "Mise en place ERP", Status.CANCELLED);
        project5 = new Project("[Test] API Gateway", user1, "API Gateway centralisée", Status.IN_PROGRESS);

        projectDAO.save(project1);
        projectDAO.save(project2);
        projectDAO.save(project3);
        projectDAO.save(project4);
        projectDAO.save(project5);

        // ==== FICHES DE PAIE ====
        payslipDAO.save(new Payslip(2024, 10, new BigDecimal("4500.00"), new BigDecimal("500.00"), new BigDecimal("1000.00"), user1));
        payslipDAO.save(new Payslip(2024, 10, new BigDecimal("2500.00"), new BigDecimal("200.00"), new BigDecimal("600.00"), user2));
        payslipDAO.save(new Payslip(2024, 10, new BigDecimal("5500.00"), new BigDecimal("1000.00"), new BigDecimal("1400.00"), user3));
        payslipDAO.save(new Payslip(2024, 10, new BigDecimal("4000.00"), new BigDecimal("300.00"), new BigDecimal("900.00"), user4));
        payslipDAO.save(new Payslip(2024, 10, new BigDecimal("2300.00"), new BigDecimal("150.00"), new BigDecimal("550.00"), user5));
    }

    @Test
    void testCreateAndFindUsers() {
        List<User> seniors = userDAO.findByGrade(Grade.SENIOR);
        assertTrue(seniors.stream().anyMatch(u -> u.getMatricule().equals("EMP001_TEST")));
    }

    @Test
    void testAssignUserToProjectAndStatusUpdate() {
        User u1 = userDAO.findById(user1.getId()).orElseThrow();
        boolean assigned = projectDAO.assignUserToProject(project1.getId(), u1.getMatricule());
        assertTrue(assigned);

        boolean statusUpdated = projectDAO.updateStatus(project1.getId(), Status.COMPLETED);
        assertTrue(statusUpdated);

        List<Project> projects = projectDAO.findByUserId(u1.getId());
        assertTrue(projects.stream().anyMatch(p -> p.getId().equals(project1.getId())));
    }

    @Test
    void testPayslipUpdate() {
        User u1 = userDAO.findById(user1.getId()).orElseThrow();
        List<Payslip> payslips = payslipDAO.findByUser(u1);
        assertFalse(payslips.isEmpty());

        Payslip p = payslips.get(0);
        p.setNetPay(new BigDecimal("4800.00"));
        payslipDAO.update(p);

        Optional<Payslip> updated = payslipDAO.findById(p.getId());
        assertTrue(updated.isPresent());
        assertEquals(new BigDecimal("4800.00"), updated.get().getNetPay());
    }

    @Test
    void testDepartmentAndPositionAssignment() {
        User u1 = userDAO.findById(user1.getId()).orElseThrow();
        u1.setDepartment(dep1);
        u1.setPosition(pos1);
        userDAO.update(u1);

        List<User> usersInDep1 = departmentDAO.findUsersByDepartment(dep1.getId());
        assertTrue(usersInDep1.stream().anyMatch(u -> u.getId().equals(u1.getId())));

        List<User> usersInPos1 = positionDAO.findUsersByPosition(pos1.getId());
        assertTrue(usersInPos1.stream().anyMatch(u -> u.getId().equals(u1.getId())));
    }

    @AfterAll
    void cleanupAndShutdown() {
        try {
            // 1️⃣ Supprimer les fiches de paie
            payslipDAO.findAll().stream()
                    .filter(p -> p.getUser() != null && p.getUser().getMatricule().endsWith("_TEST"))
                    .forEach(payslipDAO::delete);

            // 2️⃣ Supprimer les projets
            projectDAO.findAll().stream()
                    .filter(p -> p.getName().startsWith("[Test]"))
                    .forEach(projectDAO::delete);

            // 3️⃣ Supprimer les utilisateurs
            userDAO.findAll().stream()
                    .filter(u -> u.getMatricule() != null && u.getMatricule().endsWith("_TEST"))
                    .forEach(userDAO::delete);

            // 4️⃣ Supprimer départements et positions
            departmentDAO.findAll().stream()
                    .filter(d -> d.getName().startsWith("[Test]"))
                    .forEach(departmentDAO::delete);

            positionDAO.findAll().stream()
                    .filter(p -> p.getName().startsWith("[Test]"))
                    .forEach(positionDAO::delete);

            System.out.println("✅ Données de test supprimées correctement.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown(); // <- seulement à la fin
            System.out.println("✅ Session Hibernate fermée.");
        }
    }

}
