package fr.projetjee.dao;

import fr.projetjee.Model.Utilisateur;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilisateurDAO {
    
    // ✅ Maintenant par ID (Integer)
    public Utilisateur save(Utilisateur utilisateur) {
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
    
    public Utilisateur update(Utilisateur utilisateur) {
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
            Utilisateur utilisateur = session.get(Utilisateur.class, id);
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
    public Optional<Utilisateur> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Utilisateur utilisateur = session.get(Utilisateur.class, id);
            return Optional.ofNullable(utilisateur);
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    // ✅ NOUVELLE méthode : Trouver par matricule
    public Optional<Utilisateur> findByMatricule(String matricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u WHERE u.matricule = :matricule", Utilisateur.class);
            query.setParameter("matricule", matricule);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("❌ Erreur findByMatricule: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<Utilisateur> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery("FROM Utilisateur", Utilisateur.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findAll: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Optional<Utilisateur> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u WHERE u.email = :email", Utilisateur.class);
            query.setParameter("email", email);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("❌ Erreur findByEmail: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<Utilisateur> findByLastName(String lastName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u WHERE LOWER(u.lastName) LIKE LOWER(:lastName)", Utilisateur.class);
            query.setParameter("lastName", "%" + lastName + "%");
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByLastName: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Utilisateur> findByFirstName(String firstName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u WHERE LOWER(u.firstName) LIKE LOWER(:firstName)", Utilisateur.class);
            query.setParameter("firstName", "%" + firstName + "%");
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByFirstName: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Utilisateur> findByGrade(Grade grade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u WHERE u.grade = :grade", Utilisateur.class);
            query.setParameter("grade", grade);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByGrade: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Utilisateur> findByRole(Role role) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u WHERE u.role = :role", Utilisateur.class);
            query.setParameter("role", role);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByRole: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Utilisateur> findByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u WHERE u.department.id = :deptId", Utilisateur.class);
            query.setParameter("deptId", departmentId);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByDepartment: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Utilisateur> findByPosition(Integer positionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "FROM Utilisateur u WHERE u.position.id = :posId", Utilisateur.class);
            query.setParameter("posId", positionId);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByPosition: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Utilisateur> findByProject(Integer projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                "SELECT u FROM Utilisateur u JOIN u.projects p WHERE p.id = :projId", Utilisateur.class);
            query.setParameter("projId", projectId);
            return query.list();
        } catch (Exception e) {
            System.err.println("❌ Erreur findByProject: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM Utilisateur u", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("❌ Erreur count: " + e.getMessage());
            return 0;
        }
    }
    
    public long countByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM Utilisateur u WHERE u.department.id = :deptId", Long.class);
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
                "SELECT COUNT(u) FROM Utilisateur u WHERE u.grade = :grade", Long.class);
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
            Utilisateur utilisateur = session.get(Utilisateur.class, id);
            return utilisateur != null;
        } catch (Exception e) {
            System.err.println("❌ Erreur exists: " + e.getMessage());
            return false;
        }
    }
}