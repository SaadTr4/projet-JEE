package fr.projetjee;

import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.ContractType;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;
import fr.projetjee.util.PasswordUtil;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUserBatch {

    private UserDAO userDAO;
    private List<User> testUsers;

    private final int N = 200; // nombre d'utilisateurs à créer

    @BeforeAll
    void initDAO() {
        userDAO = new UserDAO();
        testUsers = new ArrayList<>();
    }

    @Test
    @Order(1)
    void testCreateNUsers() {
        for (int i = 1; i <= N; i++) {
            User user = new User("EMP_BATCH_" + i, "LastName" + i, "FirstName" + i, "user" + i + "@entreprise.fr", ContractType.ON_CALL);
            user.setPassword(PasswordUtil.hashPassword("password" + i));
            user.setAddress(i + " Rue Test, 75000 Paris");
            user.setPhone("060000000" + i);
            user.setGrade(i % 2 == 0 ? Grade.JUNIOR : Grade.SENIOR);
            user.setRole(i % 3 == 0 ? Role.ADMINISTRATEUR : Role.CHEF_PROJET);

            try {
                userDAO.save(user);
                testUsers.add(user); // garder la trace pour la suppression
            } catch (Exception e) {
                fail("Erreur lors de la création de l'utilisateur " + i + " : " + e.getMessage());
            }
        }

        assertEquals(N, testUsers.size(), "Tous les utilisateurs n'ont pas été créés correctement.");
    }

    @Test
    @Order(2)
    void testDeleteAllCreatedUsers() {
        for (User user : testUsers) {
            try {
                userDAO.delete(user);
            } catch (Exception e) {
                fail("Erreur lors de la suppression de l'utilisateur " + user.getMatricule() + " : " + e.getMessage());
            }
        }

        // Vérification que tous ont été supprimés
        for (User user : testUsers) {
            assertFalse(userDAO.findById(user.getId()).isPresent(),
                    "L'utilisateur " + user.getMatricule() + " devrait être supprimé.");
        }

        testUsers.clear();
    }

    @AfterAll
    void cleanup() {
        HibernateUtil.shutdown();
    }
}
