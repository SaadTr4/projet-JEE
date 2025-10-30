package fr.projetjee.dao;

import fr.projetjee.model.Project;
import fr.projetjee.model.Status;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class ProjectDAO {

    private SessionFactory sessionFactory;

    public ProjectDAO(){
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }
    public void saveProject(Project project) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(project);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void deleteProject(Project project) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.remove(project);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Project getProjectById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(Project.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Project getProjectByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Project p WHERE p.name = :name", Project.class)
                    .setParameter("name", name)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Project> getAllProjects() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Project", Project.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Project> getProjectsByStatus(Status status) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Project p WHERE p.status = :status", Project.class)
                    .setParameter("status", status)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
