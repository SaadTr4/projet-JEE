package fr.projetjee.dao;

import fr.projetjee.model.Payslip;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PayslipDAO extends GenericDAO<Payslip, Integer> {

    public PayslipDAO() {
        super(Payslip.class);
    }

    public List<Payslip> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Payslip> query = session.createQuery("FROM Payslip", Payslip.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("Erreur trouver toutes les fiches de paie: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Payslip> findByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Payslip> query = session.createQuery(
                    "FROM Payslip p WHERE p.user = :user", Payslip.class);
            query.setParameter("user", user);
            return query.list();
        } catch (Exception e) {
            System.err.println("Erreur trouver fiches de paie par utilisateur: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Payslip> findByMonth(User user, int year, int month) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Payslip> query = session.createQuery(
                    "FROM Payslip p WHERE p.user = :user AND p.year = :year AND p.month = :month", Payslip.class);
            query.setParameter("user", user);
            query.setParameter("year", year);
            query.setParameter("month", month);
            return query.list();
        } catch (Exception e) {
            System.err.println("Erreur trouver fiches de paie par période: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
