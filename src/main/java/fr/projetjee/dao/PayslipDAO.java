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


    /**
     * Filtrage dynamique des fiches de paie
     * @param matricule ex : "EMP001"
     * @param year ex : "2024"
     * @param month ex : "5"
     */
    public List<Payslip> findFiltered(String matricule, Integer year, Integer month) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            StringBuilder hql = new StringBuilder(
                    "SELECT p FROM Payslip p " +
                            "LEFT JOIN FETCH p.user u " +
                            "WHERE 1=1 "
            );

            if (matricule != null && !matricule.isEmpty())
                hql.append("AND u.matricule = :matricule ");

            if (year != null)
                hql.append("AND p.year = :year ");

            if (month != null)
                hql.append("AND p.month = :month ");

            hql.append("ORDER BY p.year DESC, p.month DESC");

            Query<Payslip> query = session.createQuery(hql.toString(), Payslip.class);

            if (matricule != null && !matricule.isEmpty())
                query.setParameter("matricule", matricule);

            if (year != null)
                query.setParameter("year", year);

            if (month != null)
                query.setParameter("month", month);

            return query.list();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
