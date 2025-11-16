package fr.projetjee;

import fr.projetjee.dao.DepartmentDAO;
import fr.projetjee.dao.PositionDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.model.Department;
import fr.projetjee.model.Position;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestDepartmentAndPostion {

    private DepartmentDAO departmentDAO;
    private PositionDAO positionDAO;
    private UserDAO userDAO;

    private User user1;
    private Department deptInfo, deptRH;
    private Position posDev, posManager;
    private static int deptInfoId;
    private static int deptRHId;

    private static int posDevId;
    private static int posManagerId;

    @BeforeAll
    void init() {
        departmentDAO = new DepartmentDAO();
        positionDAO = new PositionDAO();
        userDAO = new UserDAO();

        // Création des départements
        deptInfo = new Department("InformatiqueT", "IT_TEST", "Gestion du système d'information");
        deptRH = new Department("Ressources Humaines", "RH_TEST", "Gestion des ressources humaines");
        departmentDAO.save(deptInfo);
        departmentDAO.save(deptRH);
        deptInfoId = deptInfo.getId();
        deptRHId = deptRH.getId();

        // Création des postes
        posDev = new Position("DéveloppeurT", "Développement des applications");
        posManager = new Position("Manager_Test", "Gestion des équipes");
        positionDAO.save(posDev);
        positionDAO.save(posManager);
        posDevId = posDev.getId();
        posManagerId = posManager.getId();

        // Création des utilisateurs
        user1 = new User("EMP300_TEST","Nom","Louis","louis@entreprise.fr");
        user1.setPassword("test123");
        userDAO.save(user1);
    }


    // ----------------------------------------------------
    // TESTS DEPARTMENT
    // ----------------------------------------------------

    @Test @Order(1)
    void testFindByName_DepartmentExisting() {
        Optional<Department> dept = departmentDAO.findByName("InformatiqueT");
        assertTrue(dept.isPresent());
    }

    @Test @Order(2)
    void testFindByName_Department_notFound() {
        assertTrue(departmentDAO.findByName("PasDéfini").isEmpty());
    }

    @Test @Order(3)
    void testFindAll_Departments() {
        List<Department> list = departmentDAO.findAll();
        assertEquals(2, list.size());
    }

    @Test @Order(4)
    void testAssignUserToDepartmentExisting() {
        assertTrue(departmentDAO.assignUserToDepartment(deptInfoId, "EMP300_TEST"));
    }

    @Test @Order(5)
    void testAssignUserToDepartment_deptNotFound() {
        assertFalse(departmentDAO.assignUserToDepartment(9999, "EMP300_TEST"));
    }

    @Test @Order(6)
    void testAssignUserToDepartment_userNotFound() {
        assertFalse(departmentDAO.assignUserToDepartment(deptInfoId, "UNKNOWN_USER"));
    }

    @Test @Order(7)
    void testCountUsersByDepartment() {
        long count = departmentDAO.countUsersByDepartment(deptInfoId);
        assertEquals(1, count);
    }

    @Test @Order(8)
    void testFindUsersByDepartment() {
        List<User> users = departmentDAO.findUsersByDepartment(deptInfoId);
        assertEquals(1, users.size());
    }

    // ----------------------------------------------------
    // TESTS POSITION
    // ----------------------------------------------------

    @Test @Order(9)
    void testFindByName_PositionExisting() {
        Optional<Position> pos = positionDAO.findByName("DéveloppeurT");
        assertTrue(pos.isPresent());
    }

    @Test @Order(10)
    void testFindByName_Position_notFound() {
        assertTrue(positionDAO.findByName("PasDePosition").isEmpty());
    }

    @Test @Order(11)
    void testFindAll_Positions() {
        List<Position> list = positionDAO.findAll();
        assertEquals(2, list.size());
    }

    @Test @Order(12)
    void testAssignUserToPositionExisting() {
        assertTrue(positionDAO.assignUserToPosition(posDevId, "EMP300_TEST"));
    }

    @Test @Order(13)
    void testAssignUserToPosition_positionNotFound() {
        assertFalse(positionDAO.assignUserToPosition(9876, "EMP300_TEST"));
    }

    @Test @Order(14)
    void testAssignUserToPosition_userNotFound() {
        assertFalse(positionDAO.assignUserToPosition(posDevId, "UNKNOWN"));
    }

    @Test @Order(15)
    void testCountUsersByPosition() {
        long count = positionDAO.countUsersByPosition(posDevId);
        assertEquals(1, count);
    }

    @Test @Order(16)
    void testFindUsersByPosition() {
        List<User> users = positionDAO.findUsersByPosition(posDevId);
        assertEquals(1, users.size());
    }

    @Test @Order(17)
    void cleanupUser() {
        userDAO.delete(user1);
        assertTrue(userDAO.findById(user1.getId()).isEmpty());
    }
    @Test @Order(18)
    void testDeleteDepartmentAndPosition() {

        departmentDAO.delete(deptInfo);
        departmentDAO.delete(deptRH);

        positionDAO.delete(posDev);
        positionDAO.delete(posManager);

        // Vérification de la suppression
        assertTrue(departmentDAO.findById(deptInfoId).isEmpty());
        assertTrue(departmentDAO.findById(deptRHId).isEmpty());
        assertTrue(positionDAO.findById(posDevId).isEmpty());
        assertTrue(positionDAO.findById(posManagerId).isEmpty());
    }
}

