package fr.projetjee;

import fr.projetjee.dao.ProjectDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.Status;
import fr.projetjee.enums.Role;
import fr.projetjee.model.Project;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestProject {

    private ProjectDAO projectDAO;
    private UserDAO userDAO;

    private Project project1, project2;
    private User user1, user2;

    @BeforeAll
    void initDAO() {
        projectDAO = new ProjectDAO();
        userDAO = new UserDAO();

        // Création des users
        user1 = new User("EMP100_TEST", "DupontTest", "Jean", "jean.dupontTest@entreprise.fr");
        user1.setPassword("test123");
        user1.setRole(Role.CHEF_PROJET);
        userDAO.save(user1);

        user2 = new User("EMP101_TEST", "MartinTest", "Sophie", "sophie.martinTest@entreprise.fr");
        user2.setPassword("test123");
        user2.setRole(Role.EMPLOYE);
        userDAO.save(user2);

        // Création des projets
        project1 = new Project("Projet Alpha", user1, "Alpha description", Status.PLANNED);
        projectDAO.save(project1);

        project2 = new Project("Projet Beta", user1, "Beta description", Status.IN_PROGRESS);
        projectDAO.save(project2);
    }

    // ===============================
    // Tests CRUD
    // ===============================

    @Test
    @Order(1)
    void testSaveProject_success() {
        Project project = new Project("Projet Gamma", user1, "Gamma description", Status.PLANNED);
        assertDoesNotThrow(() -> projectDAO.save(project));
        projectDAO.delete(project);
    }

    @Test
    @Order(2)
    void testFindById_existingProject() {
        Optional<Project> found = projectDAO.findById(project1.getId());
        assertTrue(found.isPresent());
        assertEquals("Projet Alpha", found.get().getName());
    }

    @Test
    @Order(3)
    void testFindById_nonExistingProject() {
        Optional<Project> found = projectDAO.findById(99999);
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(4)
    void testFindByName_existingProject() {
        Optional<Project> found = projectDAO.findByName("Projet Alpha");
        assertTrue(found.isPresent());
        assertEquals(project1.getId(), found.get().getId());
    }

    @Test
    @Order(5)
    void testFindByName_nonExistingProject() {
        Optional<Project> found = projectDAO.findByName("Projet Inexistant");
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(6)
    void testUpdateStatus_existingProject() {
        boolean result = projectDAO.updateStatus(project1.getId(), Status.IN_PROGRESS);
        assertTrue(result);
        Project updated = projectDAO.findById(project1.getId()).get();
        assertEquals(Status.IN_PROGRESS, updated.getStatus());
    }

    @Test
    @Order(7)
    void testUpdateStatus_nonExistingProject() {
        boolean result = projectDAO.updateStatus(99999, Status.COMPLETED);
        assertFalse(result);
    }

    // ===============================
    // Tests assignation users
    // ===============================

    @Test
    @Order(8)
    void testAssignUserToProject_success() {
        boolean result = projectDAO.assignUserToProject(project2.getId(), user2.getMatricule());
        assertTrue(result);

        Project updated = projectDAO.findByIdWithUsers(project2.getId()).get();
        assertTrue(updated.getUsers().contains(user2));
    }

    @Test
    @Order(9)
    void testAssignUserToProject_nonExistingProject() {
        boolean result = projectDAO.assignUserToProject(99999, user2.getMatricule());
        assertFalse(result);
    }

    @Test
    @Order(10)
    void testAssignUserToProject_nonExistingUser() {
        boolean result = projectDAO.assignUserToProject(project1.getId(), "FAKE_USER");
        assertFalse(result);
    }

    @Test
    @Order(11)
    void testRemoveUserFromProject_success() {
        projectDAO.assignUserToProject(project1.getId(), user2.getMatricule());
        boolean result = projectDAO.removeUserFromProject(project1.getId(), user2.getMatricule());
        assertTrue(result);
    }

    @Test
    @Order(12)
    void testRemoveUserFromProject_nonExistingProjectOrUser() {
        boolean result = projectDAO.removeUserFromProject(99999, "FAKE_USER");
        assertFalse(result);
    }

    // ===============================
    // Tests project manager
    // ===============================

    @Test
    @Order(13)
    void testUpdateProjectManager_success() {
        boolean result = projectDAO.updateProjectManager(project2.getId(), user1.getMatricule());
        assertTrue(result);

        Project updated = projectDAO.findById(project2.getId()).get();
        assertEquals(user1.getId(), updated.getProjectManager().getId());
    }

    @Test
    @Order(14)
    void testUpdateProjectManager_nonExistingProject() {
        boolean result = projectDAO.updateProjectManager(99999, user1.getMatricule());
        assertFalse(result);
    }

    @Test
    @Order(15)
    void testUpdateProjectManager_nonExistingUser() {
        boolean result = projectDAO.updateProjectManager(project1.getId(), "FAKE_USER");
        assertFalse(result);
    }

    // ===============================
    // Tests find with filters
    // ===============================

    @Test
    @Order(16)
    void testFindProjectsWithFilters_byName() {
        List<Project> projects = projectDAO.findProjectsWithFilters("Alpha", null, null);
        assertEquals(1, projects.size());
        assertEquals(project1.getId(), projects.get(0).getId());
    }

    @Test
    @Order(17)
    void testFindProjectsWithFilters_byManager() {
        List<Project> projects = projectDAO.findProjectsWithFilters(null, user1.getMatricule(), null);
        assertTrue(projects.stream().anyMatch(p -> p.getProjectManager().getId().equals(user1.getId())));
    }

    @Test
    @Order(18)
    void testFindProjectsWithFilters_byStatus() {
        List<Project> projects = projectDAO.findProjectsWithFilters(null, null, Status.IN_PROGRESS);
        assertTrue(projects.stream().allMatch(p -> p.getStatus() == Status.IN_PROGRESS));
    }

    @Test
    @Order(19)
    void testFindProjectsWithFilters_combined() {
        List<Project> projects = projectDAO.findProjectsWithFilters("Alpha", user1.getMatricule(), Status.IN_PROGRESS);
        assertEquals(1, projects.size());
        assertEquals(project1.getId(), projects.get(0).getId());
    }

    // ===============================
    // Tests findAll
    // ===============================

    @Test
    @Order(20)
    void testFindAll_nonEmptyDatabase() {
        List<Project> projects = projectDAO.findAll();
        assertTrue(projects.size() >= 2);
    }

    @Test
    @Order(21)
    void testDeleteProject_success() {
        assertDoesNotThrow(() -> projectDAO.delete(project1));
        assertDoesNotThrow( () -> projectDAO.delete(project2));
    }

    @Test
    @Order(22)
    void testFindAll_emptyDatabase() {
        List<Project> projects = projectDAO.findAll();
        assertEquals(0, projects.size());
    }

    @Test
    @Order(23)
    void cleanupUsers() {
        userDAO.delete(user1);
        userDAO.delete(user2);
    }

    // ===============================
    // Teardown
    // ===============================

    @AfterAll
    void shutdown() {
        HibernateUtil.shutdown();
    }
}
