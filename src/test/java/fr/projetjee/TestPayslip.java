package fr.projetjee;

import fr.projetjee.dao.PayslipDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.ContractType;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.model.Payslip;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPayslip {

    private PayslipDAO payslipDAO;
    private UserDAO userDAO;

    private User user1, user2;
    private Payslip payslip1, payslip2;

    @BeforeAll
    void initDAO() {

        payslipDAO = new PayslipDAO();
        userDAO = new UserDAO();

        // ---------------------
        // Création des utilisateurs
        // ---------------------
        user1 = new User("EMP200_TEST", "DurandTest", "Paul", "paul.durandTest@entreprise.fr", ContractType.APPRENTICESHIP);
        user1.setPassword("test123");
        user1.setGrade(Grade.SENIOR);
        user1.setRole(Role.EMPLOYE);
        user1.setBaseSalary(new BigDecimal("4000.00"));
        userDAO.save(user1);

        user2 = new User("EMP201_TEST", "BernardTest", "Lucie", "lucie.bernardTest@entreprise.fr", ContractType.INTERNSHIP);
        user2.setPassword("test123");
        user2.setGrade(Grade.JUNIOR);
        user2.setRole(Role.EMPLOYE);
        user2.setBaseSalary(new BigDecimal("2800.00"));
        userDAO.save(user2);

        // ---------------------
        // Création des fiches de paie
        // ---------------------
        payslip1 = new Payslip(2024,5, new BigDecimal("350.00"), new BigDecimal("750.00"), user1);
        payslipDAO.save(payslip1);

        payslip2 = new Payslip(2024,6, new BigDecimal("400.00"), new BigDecimal("800.00"), user1);
        payslipDAO.save(payslip2);
    }

    // ============================
    // Tests CRUD
    // ============================

    @Test
    @Order(1)
    void testSavePayslip_success() {
        Payslip p = new Payslip(2024, 10, new BigDecimal("500.00"), new BigDecimal("1000.00"), user1);
        assertDoesNotThrow(() -> payslipDAO.save(p));
        payslipDAO.delete(p);
    }

    @Test
    @Order(2)
    void testFindById_existing() {
        var result = payslipDAO.findById(payslip1.getId());
        assertTrue(result.isPresent());
        assertEquals(5, result.get().getMonth());
    }

    @Test
    @Order(3)
    void testFindById_nonExisting() {
        assertTrue(payslipDAO.findById(99999).isEmpty());
    }

    @Test
    @Order(4)
    void testUpdatePayslip_success() {
        payslip1.setBaseSalary(new BigDecimal("3500.00"));
        payslipDAO.update(payslip1);

        Payslip updated = payslipDAO.findById(payslip1.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(3500.00, updated.getBaseSalary().doubleValue());
    }

    @Test
    @Order(5)
    void testUpdatePayslip_nonExisting() {
        Payslip fakeSlip = new Payslip(2023, 1, new BigDecimal("200.00"), new BigDecimal("500.00"), user2);
        fakeSlip.setId(999999);

        assertThrows(Exception.class, () -> payslipDAO.update(fakeSlip));
    }

    // ============================
    // Tests findAll
    // ============================

    @Test
    @Order(6)
    void testFindAll_nonEmpty() {
        List<Payslip> all = payslipDAO.findAll();
        assertTrue(all.size() >= 2);
    }

    // ============================
    // Tests findByUser / findByUserId
    // ============================

    @Test
    @Order(7)
    void testFindByUser_existingUser() {
        List<Payslip> slips = payslipDAO.findByUser(user1);
        assertEquals(2, slips.size());
    }

    @Test
    @Order(8)
    void testFindByUserId_existing() {
        List<Payslip> slips = payslipDAO.findByUserId(user1.getId());
        assertEquals(2, slips.size());
    }

    @Test
    @Order(9)
    void testFindByUserId_nonExisting() {
        List<Payslip> slips = payslipDAO.findByUserId(999999);
        assertTrue(slips.isEmpty());
    }

    // ============================
    // Tests findByMonth
    // ============================

    @Test
    @Order(10)
    void testFindByMonth_valid() {
        List<Payslip> slips = payslipDAO.findByMonth(user1, 2024, 5);
        assertEquals(1, slips.size());
        assertEquals(payslip1.getId(), slips.get(0).getId());
    }

    @Test
    @Order(11)
    void testFindByMonth_noResult() {
        List<Payslip> slips = payslipDAO.findByMonth(user1, 2025, 1);
        assertTrue(slips.isEmpty());
    }

    // ============================
    // Tests findFiltered
    // ============================

    @Test
    @Order(12)
    void testFindFiltered_byMatricule() {
        List<Payslip> slips = payslipDAO.findFiltered(user1.getMatricule(), null, null);
        assertEquals(2, slips.size());
    }

    @Test
    @Order(13)
    void testFindFiltered_byYear() {
        List<Payslip> slips = payslipDAO.findFiltered(null, 2024, null);
        assertEquals(2, slips.size());
    }

    @Test
    @Order(14)
    void testFindFiltered_byMonth() {
        List<Payslip> slips = payslipDAO.findFiltered(null, null, 5);
        assertEquals(1, slips.size());
    }

    @Test
    @Order(15)
    void testFindFiltered_combined() {
        List<Payslip> slips = payslipDAO.findFiltered(user1.getMatricule(), 2024, 6);
        assertEquals(1, slips.size());
        assertEquals(payslip2.getId(), slips.get(0).getId());
    }

    // ============================
    // Test existence d’une fiche
    // ============================

    @Test
    @Order(16)
    void testExistsPayslip_true() {
        assertTrue(payslipDAO.existsPayslipForUserAndMonth(user1, 2024, 5));
    }

    @Test
    @Order(17)
    void testExistsPayslip_false() {
        assertFalse(payslipDAO.existsPayslipForUserAndMonth(user2, 2030, 12));
    }

    // ============================
    // Tests delete
    // ============================

    @Test
    @Order(18)
    void testDeletePayslip() {
        payslipDAO.delete(payslip1);
        payslipDAO.delete(payslip2);

        assertTrue(payslipDAO.findByUser(user1).isEmpty());
    }

    @Test
    @Order(19)
    void testFindAll_emptyAfterDelete() {
        List<Payslip> slips = payslipDAO.findAll();
        assertTrue(slips.isEmpty());
    }

    @Test
    @Order(20)
    void cleanupUsers() {
        userDAO.delete(user1);
        userDAO.delete(user2);
    }

    @AfterAll
    void shutdown() {
        HibernateUtil.shutdown();
    }
}
