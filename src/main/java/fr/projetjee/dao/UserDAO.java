package fr.projetjee.dao;

import fr.projetjee.model.User;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    
    // ✅ Maintenant par ID (Integer)
    public User save(User utilisateur) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(utilisateur);
            transaction.commit();
            System.out.println("✅ Utilisateur créé: ID=" + utilisateur.getId() + ", Matricule=" + utilisateur.getMatricule());
            return utilisateur;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur création: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public User update(User utilisateur) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(utilisateur);
            transaction.commit();
            System.out.println("✅ Utilisateur mis à jour: ID=" + utilisateur.getId());
            return utilisateur;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur mise à jour: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // ✅ Supprimer par ID (Integer)
    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User utilisateur = session.find(User.class, id);
            if (utilisateur != null) {
                session.remove(utilisateur);
                transaction.commit();
                System.out.println("✅ Utilisateur supprimé: ID=" + id);
                return true;
            }
            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("❌ Erreur suppression: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ✅ Trouver par ID
    public Optional<User> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User utilisateur = session.find(User.class, id);
            return Optional.ofNullable(utilisateur);
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    // ✅ NOUVELLE méthode : Trouver par matricule
    public Optional<User> findByMatricule(String matricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.matricule = :matricule", User.class);
            query.setParameter("matricule", matricule);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("❌ Erreur findByMatricule: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findAll: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("❌ Erreur findByEmail: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<User> findByLastName(String lastName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:lastName)", User.class);
            query.setParameter("lastName", "%" + lastName + "%");
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByLastName: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<User> findByFirstName(String firstName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE LOWER(u.firstName) LIKE LOWER(:firstName)", User.class);
            query.setParameter("firstName", "%" + firstName + "%");
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByFirstName: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<User> findByGrade(Grade grade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.grade = :grade", User.class);
            query.setParameter("grade", grade);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByGrade: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<User> findByRole(Role role) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.role = :role", User.class);
            query.setParameter("role", role);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByRole: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
   public List<User> findByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.department.id = :deptId", User.class);
            query.setParameter("deptId", departmentId);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByDepartment: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<User> findByPosition(Integer positionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.position.id = :posId", User.class);
            query.setParameter("posId", positionId);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByPosition: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<User> findByProject(Integer projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "SELECT u FROM User u JOIN u.projects p WHERE p.id = :projId", User.class);
            query.setParameter("projId", projectId);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByProject: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("❌ Erreur count: " + e.getMessage());
            return 0;
        }
    }
    
    public long countByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.department.id = :deptId", Long.class);
            query.setParameter("deptId", departmentId);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("❌ Erreur countByDepartment: " + e.getMessage());
            return 0;
        }
    }
    
    public long countByGrade(Grade grade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.grade = :grade", Long.class);
            query.setParameter("grade", grade);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("❌ Erreur countByGrade: " + e.getMessage());
            return 0;
        }
    }
    
    // ✅ Vérifier existence par ID
    public boolean exists(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User utilisateur = session.find(User.class, id);
            return utilisateur != null;
        } catch (Exception e) {
            System.err.println("❌ Erreur exists: " + e.getMessage());
            return false;
        }
    }
}