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

public class PayslipDAO {

    public Payslip save(Payslip payslip) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(payslip);
            transaction.commit();
            return payslip;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Erreur sauvegarde fiche de paie: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Payslip payslip = session.find(Payslip.class, id);
            if (payslip != null) {
                session.remove(payslip);
                transaction.commit();
                return true;
            }
            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Erreur suppression fiche de paie: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void update(Payslip payslip) {
        save(payslip);
    }

    public Optional<Payslip> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Payslip payslip = session.find(Payslip.class, id);
            return Optional.ofNullable(payslip);
        } catch (Exception e) {
            System.err.println("Erreur trouver fiche de paie par ID: " + e.getMessage());
            return Optional.empty();
        }
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
                    "FROM Payslip p WHERE p.user = :user AND year(p.date) = :year AND month(p.date) = :month", Payslip.class);
            query.setParameter("user", user);
            query.setParameter("year", year);
            query.setParameter("month", month);
            return query.list();
        } catch (Exception e) {
            System.err.println("Erreur trouver fiches de paie par mois: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
