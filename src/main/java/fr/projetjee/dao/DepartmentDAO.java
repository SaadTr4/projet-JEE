package fr.projetjee.dao;

import fr.projetjee.model.Department;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDAO extends GenericDAO<Department, Integer> {

    public DepartmentDAO() {
        super(Department.class);
    }

    public Optional<Department> findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Department> query = session.createQuery(
                    "FROM Department d WHERE d.name = :name", Department.class);
            query.setParameter("name", name);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver département par nom: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Department> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Department> query = session.createQuery("FROM Department", Department.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver tous les départements: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public List<User> findUsersByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.department.id = :deptId", User.class);
            query.setParameter("deptId", departmentId);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver utilisateurs par département: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public long countUsersByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.department.id = :deptId", Long.class);
            query.setParameter("deptId", departmentId);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur compter utilisateurs par département: " + e.getMessage());
            return 0;
        }
    }

    public boolean assignUserToDepartment(Integer departmentId, String registrationNumber) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Department department = session.find(Department.class, departmentId);
            User user = session.createQuery("FROM User u WHERE u.matricule = :reg", User.class)
                    .setParameter("reg", registrationNumber)
                    .uniqueResult();

            if (department != null && user != null) {
                user.setDepartment(department);
                session.merge(user);
                transaction.commit();
                System.out.println("[SUCCESS][DAO] Utilisateur " + registrationNumber + " affecté au département ID=" + departmentId);
                return true;
            } else {
                if (transaction != null) transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[ERROR][DAO] Erreur affectation utilisateur au département: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    }
