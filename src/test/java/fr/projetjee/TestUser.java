package fr.projetjee;

import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.ContractType;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUser {

    private UserDAO userDAO ;

    private User user1, user2;

    @BeforeAll
    void initDAO() {
        userDAO = new UserDAO();

        user1 = new User("EMP001_TEST", "DupontTest", "Jean", "jean.dupontTest@entreprise.fr", ContractType.APPRENTICESHIP);
        user1.setPassword("test123");
        user1.setAddress("123 Rue de la Paix, 75002 Paris");
        user1.setPhone("0601020304");
        user1.setGrade(Grade.SENIOR);
        user1.setRole(Role.CHEF_PROJET);
        userDAO.save(user1);

        user2 = new User("EMP002_TEST", "MartinTest", "Sophie", "sophie.martinTest@entreprise.fr", ContractType.PERMANENT_FULL_TIME);
        user2.setPassword("test123");
        user2.setAddress("456 Avenue des Champs, 75008 Paris");
        user2.setPhone("0602030405");
        user2.setGrade(Grade.JUNIOR);
        user2.setRole(Role.EMPLOYE);
        userDAO.save(user2);
    }

    // ========================================
    // Test CRUD pour UserDAO
    // ========================================

    @Test
    @Order(1)
    // Vérifie que save fonctionne pour un utilisateur valide.
    void testSaveUser_success() {
        User testUser = new User("EMP007_TEST", "TestUser", "Alice", "alice.test@entreprise.fr", ContractType.PERMANENT_FULL_TIME);
        testUser.setPassword("password123");
        testUser.setAddress("10 Rue Test, 75010 Paris");
        testUser.setPhone("0607080910");
        testUser.setGrade(Grade.JUNIOR);
        testUser.setRole(Role.EMPLOYE);

        try {
            userDAO.save(testUser);  // Si ça lance une exception, le test échouera
        } catch (Exception e) {
            fail("La sauvegarde de l'utilisateur a échoué avec une exception : " + e.getMessage());
        }

        // Nettoyage : suppression de l'utilisateur après le test
        try {
            userDAO.delete(testUser);
        } catch (Exception e) {
            // Si la suppression échoue, on ignore ou on log
        }
    }

    @Test
    @Order(2)
    // Vérifie que save lance une exception pour un utilisateur null.
    void testSaveUser_failure_nullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.save(null);
        }, "La sauvegarde d'un utilisateur null devrait lancer une IllegalArgumentException.");
    }

    @Test
    @Order(3)
    // Vérifie que findById retourne Optional.empty().
    void testFindById_nonExistingUser() {
        Optional<User> foundUser = userDAO.findById(99999); // ID improbable
        assertTrue(foundUser.isEmpty(), "Aucun utilisateur ne devrait être trouvé avec un ID inexistant.");
    }

    @Test
    @Order(4)
    // Vérifie que la recherche par ID fonctionne pour un utilisateur existant.
    void testFindUserById_existingUser(){
        try {
            User foundUser = userDAO.findById(user1.getId()).orElse(null);
            assertNotNull(foundUser, "L'utilisateur devrait être trouvé par ID.");
            assertEquals("Jean", foundUser.getFirstName(), "Le prénom de l'utilisateur ne correspond pas.");
        } catch (Exception e) {
            fail("La recherche de l'utilisateur par ID a échoué avec une exception : " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    void testUpdateUser_success() {
        try {
            user1.setAddress("456 Avenue des Champs, 75008 Paris");
            userDAO.update(user1);
            User updatedUser = userDAO.findById(user1.getId()).orElse(null);
            assertNotNull(updatedUser, "L'utilisateur mis à jour devrait exister.");
            assertEquals("456 Avenue des Champs, 75008 Paris", updatedUser.getAddress(), "L'adresse mise à jour ne correspond pas.");
        } catch (Exception e) {
            fail("La mise à jour de l'utilisateur a échoué avec une exception : " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    void testUpdateUser_nonExistingUser() {
        User nonExistingUser = new User("EMP999_TEST", "NonExistant", "User", "non.existant@entreprise.fr", ContractType.PERMANENT_FULL_TIME);
        assertThrows(Exception.class, () -> userDAO.update(nonExistingUser), "La mise à jour d'un utilisateur inexistant devrait lancer une exception.");
    }

    @Test
    @Order(7)
    void testFindByMatricule_existingMatricule() {
        Optional<User> foundUser = userDAO.findByMatricule(user1.getMatricule());
        assertTrue(foundUser.isPresent(), "Un utilisateur devrait être trouvé avec un matricule existant.");
    }

    @Test
    @Order(8)
    void testFindByMatricule_nonExistingMatricule() {
        Optional<User> foundUser = userDAO.findByMatricule("NON_EXISTING");
        assertTrue(foundUser.isEmpty(), "Aucun utilisateur ne devrait être trouvé avec un matricule inexistant.");
    }

    @Test
    @Order(9)
    void testFindByEmail_existingEmail() {
        Optional<User> foundUser = userDAO.findByEmail(user1.getEmail());
        assertTrue(foundUser.isPresent(), "Un utilisateur devrait être trouvé avec un email existant.");
    }

    @Test
    @Order(10)
    void testFindByEmail_nonExistingEmail() {
        Optional<User> foundUser = userDAO.findByEmail("non.existant@entreprise.fr");
        assertTrue(foundUser.isEmpty(), "Aucun utilisateur ne devrait être trouvé avec un email inexistant.");
    }

    @Test
    @Order(11)
    void testFindByFirstName_partialMatch() {
        List<User> users = userDAO.findByFirstName("jean");
        assertFalse(users.isEmpty(), "Des utilisateurs devraient être trouvés avec une correspondance partielle insensible à la casse.");
    }

    @Test
    @Order(12)
    void testFindByFirstName_noMatch() {
        List<User> users = userDAO.findByFirstName("NonExistant");
        assertTrue(users.isEmpty(), "Aucun utilisateur ne devrait être trouvé avec un prénom inexistant.");
    }

    @Test
    @Order(13)
    void testFindByLastName_partialMatch() {
        List<User> users = userDAO.findByLastName("dupont");
        assertFalse(users.isEmpty(), "Des utilisateurs devraient être trouvés avec une correspondance partielle insensible à la casse.");
    }

    @Test
    @Order(14)
    void testFindByLastName_noMatch() {
        List<User> users = userDAO.findByLastName("NonExistant");
        assertTrue(users.isEmpty(), "Aucun utilisateur ne devrait être trouvé avec un nom inexistant.");
    }

    @Test
    @Order(15)
    void testFindByGrade_existingGrade() {
        List<User> users = userDAO.findByGrade(Grade.SENIOR);
        assertFalse(users.isEmpty(), "Des utilisateurs devraient être trouvés avec un grade existant.");
    }

    @Test
    @Order(16)
    void testFindByGrade_noUsers() {
        List<User> users = userDAO.findByGrade(Grade.EXPERT);
        assertTrue(users.isEmpty(), "Aucun utilisateur ne devrait être trouvé avec un grade inexistant.");
    }

    @Test
    @Order(17)
    void testFindByRole_existingRole() {
        List<User> users = userDAO.findByRole(Role.CHEF_PROJET);
        assertFalse(users.isEmpty(), "Des utilisateurs devraient être trouvés avec un rôle existant.");
    }

    @Test
    @Order(18)
    void testFindByRole_noUsers() {
        List<User> users = userDAO.findByRole(Role.ADMINISTRATEUR);
        assertTrue(users.isEmpty(), "Aucun utilisateur ne devrait être trouvé avec un rôle inexistant.");
    }

    @Test
    @Order(19)
    void testFindByEmailOrMatricule_matchingEmail() {
        Optional<User> result = userDAO.findByEmailOrMatricule(user1.getEmail());
        assertTrue(result.isPresent());
        assertEquals(user1.getId(), result.get().getId());
    }

    @Test
    @Order(20)
    void testFindByEmailOrMatricule_matchingMatricule() {
        Optional<User> result = userDAO.findByEmailOrMatricule(user1.getMatricule());
        assertTrue(result.isPresent());
        assertEquals(user1.getId(), result.get().getId());
    }

    @Test
    @Order(21)
    void testFindByEmailOrMatricule_noMatch() {
        Optional<User> result = userDAO.findByEmailOrMatricule("nothing");
        assertTrue(result.isEmpty());
    }

    @Test
    @Order(22)
    void testIsUserProjectManager_true() {
        boolean result = userDAO.isUserProjectManager(Role.CHEF_PROJET, user1.getId());
        assertTrue(result);
    }

    @Test
    @Order(23)
    void testIsUserProjectManager_falseWrongRole() {
        boolean result = userDAO.isUserProjectManager(Role.CHEF_PROJET, user2.getId());
        assertFalse(result);
    }

    @Test
    @Order(24)
    void testIsUserProjectManager_falseNonExistingUser() {
        boolean result = userDAO.isUserProjectManager(Role.CHEF_PROJET, 99999);
        assertFalse(result);
    }

    @Test
    @Order(25)
    void testCount_totalUsers() {
        assertEquals(2, userDAO.count());
    }

    @Test
    @Order(26)
    void testCountByDepartment_nonExistingDepartment() {
        assertEquals(0, userDAO.countByDepartment(9999));
    }



    @Test
    @Order(27)
    void testCountByGrade_existingGrade() {
        assertEquals(1, userDAO.countByGrade(Grade.SENIOR));
    }

    @Test
    @Order(28)
    void testCountByGrade_noUsers() {
        assertEquals(0, userDAO.countByGrade(Grade.EXPERT));
    }

    @Test
    @Order(29)
    void testExists_existingUser() {
        assertTrue(userDAO.exists(user1.getId()));
    }

    @Test
    @Order(30)
    void testExists_nonExistingUser() {
        assertFalse(userDAO.exists(12345));
    }

    @Test
    @Order(31)
    void testFindAll_nonEmptyDatabase() {
        List<User> users = userDAO.findAll();
        assertEquals(2, users.size());
    }

    @Test
    @Order(32)
    void testDeleteUser_existingUser() {
        try {
            userDAO.delete(user1);
            assertFalse(userDAO.findById(user1.getId()).isPresent(), "L'utilisateur 1 devrait être supprimé.");
            userDAO.delete(user2);
            assertFalse(userDAO.findById(user2.getId()).isPresent(), "L'utilisateur 2 devrait être supprimé.");
        } catch (Exception e) {
            fail("La suppression de l'utilisateur 1 et 2 a échoué avec une exception : " + e.getMessage());
        }
    }

    @Test
    @Order(33)
    void testFindAll_emptyDatabase() {
        List<User> users = userDAO.findAll();
        assertTrue(users.isEmpty());
    }

    @AfterAll
    void cleanupAndShutdown() {
        HibernateUtil.shutdown(); // <- seulement à la fin
    }

}
