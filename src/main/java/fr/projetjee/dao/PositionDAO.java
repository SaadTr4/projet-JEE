package fr.projetjee.dao;

import fr.projetjee.model.Position;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PositionDAO extends GenericDAO<Position, Integer> {

    public PositionDAO() {
        super(Position.class);
    }

    public Optional<Position> findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Position> query = session.createQuery(
                    "FROM Position p WHERE p.name = :name", Position.class);
            query.setParameter("name", name);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver position par nom: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Position> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Position> query = session.createQuery("FROM Position", Position.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver toutes les positions: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public List<User> findUsersByPosition(Integer positionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.position.id = :positionId", User.class);
            query.setParameter("positionId", positionId);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur trouver utilisateurs par position: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public long countUsersByPosition(Integer positionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.position.id = :positionId", Long.class);
            query.setParameter("positionId", positionId);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("❌ Erreur compter utilisateurs par position: " + e.getMessage());
            return 0;
        }
    }

    public boolean assignUserToPosition(Integer positionId, String registrationNumber) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Position position = session.find(Position.class, positionId);
            User user = session.createQuery("FROM User u WHERE u.matricule = :reg", User.class)
                    .setParameter("reg", registrationNumber)
                    .uniqueResult();

            if (position != null && user != null) {
                user.setPosition(position);
                session.merge(user);
                transaction.commit();
                System.out.println("✅ Utilisateur " + registrationNumber + " affecté à la position ID=" + positionId);
                return true;
            } else {
                if (transaction != null) transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur affectation utilisateur à la position: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }

    }

