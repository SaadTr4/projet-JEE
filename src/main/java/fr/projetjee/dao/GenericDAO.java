package fr.projetjee.dao;

import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO générique qui encapsule les opérations CRUD communes
 * @param <T> Le type de l'entité
 * @param <ID> Le type de l'identifiant
 */
public class GenericDAO<T, ID> {

    private final Class<T> entityClass;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Sauvegarde ou met à jour une entité
     */
    public T save(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
            System.out.println("✅ " + entityClass.getSimpleName() + " sauvegardé: " + entity);
            return entity;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur sauvegarde " + entityClass.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Persiste une nouvelle entité (INSERT)
     */
    public T persist(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            System.out.println("✅ " + entityClass.getSimpleName() + " persisté: " + entity);
            return entity;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur persistence " + entityClass.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Supprime une entité par son ID
     */
    public boolean deleteById(ID id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T entity = session.find(entityClass, id);
            if (entity != null) {
                session.remove(entity);
                transaction.commit();
                System.out.println("✅ " + entityClass.getSimpleName() + " supprimé: ID=" + id);
                return true;
            }
            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur suppression " + entityClass.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime une entité
     */
    public boolean delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
            System.out.println("✅ " + entityClass.getSimpleName() + " supprimé: " + entity);
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur suppression " + entityClass.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Met à jour une entité
     */
    public T update(T entity) {
        return save(entity);
    }

    /**
     * Trouve une entité par son ID
     */
    public Optional<T> findById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.find(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche " + entityClass.getSimpleName() + " par ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Trouve toutes les entités
     */
    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche toutes les " + entityClass.getSimpleName() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Trouve des entités avec une requête HQL personnalisée
     */
    public List<T> findByQuery(String hql, Object... parameters) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche par requête " + entityClass.getSimpleName() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Trouve une entité unique avec une requête HQL personnalisée
     */
    public Optional<T> findUniqueByQuery(String hql, Object... parameters) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche unique par requête " + entityClass.getSimpleName() + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Compte toutes les entités
     */
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("❌ Erreur comptage " + entityClass.getSimpleName() + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Compte avec une requête HQL personnalisée
     */
    public long countByQuery(String hql, Object... parameters) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(hql, Long.class);
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("❌ Erreur comptage par requête " + entityClass.getSimpleName() + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Vérifie si une entité existe par son ID
     */
    public boolean exists(ID id) {
        return findById(id).isPresent();
    }

    /**
     * Exécute une opération en transaction
     */
    public <R> R executeInTransaction(TransactionOperation<R> operation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            R result = operation.execute(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur exécution transaction " + entityClass.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Interface fonctionnelle pour les opérations en transaction
     */
    @FunctionalInterface
    public interface TransactionOperation<R> {
        R execute(Session session);
    }

    /**
     * Méthode utilitaire pour créer des requêtes nommées
     */
    public Query<T> createNamedQuery(Session session, String queryName) {
        return session.createNamedQuery(queryName, entityClass);
    }

    /**
     * Méthode utilitaire pour créer des requêtes HQL
     */
    public Query<T> createQuery(Session session, String hql) {
        return session.createQuery(hql, entityClass);
    }
}