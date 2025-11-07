package fr.projetjee.dao;

import fr.projetjee.model.Project;
import fr.projetjee.enums.Status;
import fr.projetjee.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import fr.projetjee.util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDAO {

    public Project save(Project project) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(project);
            transaction.commit();
            System.out.println("[SUCCESS][DAO] Project sauvegardé: ID=" + project.getId() + ", Nom=" + project.getName());
            return project;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[ERROR][DAO] Erreur sauvegarde projet: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Project project = session.find(Project.class, id);
            if (project != null) {
                session.remove(project);
                transaction.commit();
                System.out.println("[SUCCESS][DAO] Projet supprimé: ID=" + id);
                return true;
            }
            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[ERROR][DAO] Erreur suppression projet: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public Project update(Project project) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(project);
            transaction.commit();
            System.out.println("[SUCCESS][DAO] Project mis à jour: ID=" + project.getId() + ", Nom=" + project.getName());
            return project;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[ERROR][DAO] Erreur sauvegarde projet: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public Optional<Project> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Project project = session.find(Project.class, id);
            return Optional.ofNullable(project);
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver par ID projet: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Project> findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Project> query = session.createQuery(
                    "FROM Project p WHERE p.name = :name", Project.class);
            query.setParameter("name", name);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver par nom projet: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Project> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Charge les projets + users en une seule requête
            List<Project> projects = session.createQuery(
                    "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.users", Project.class
            ).list();
            return projects;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver tous les projets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Project> findByStatus(Status status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Project> query = session.createQuery(
                    "FROM Project p WHERE p.status = :status", Project.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur trouver pas état projets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean updateStatus(Integer projectId, Status newStatus) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Project project = session.find(Project.class, projectId);
            if (project != null) {
                project.setStatus(newStatus);
                session.merge(project);
                transaction.commit();
                System.out.println("[SUCCESS][DAO] Statut projet mis à jour: ID=" + projectId + ", Nouveau statut=" + newStatus);
                return true;
            } else {
                transaction.commit();
                System.out.println("[INFO][DAO] Projet introuvable pour mise à jour du statut: ID=" + projectId);
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[ERROR][DAO] Erreur mise à jour statut projet: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public List<Project> findByUserId(Integer userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Project> query = session.createQuery(
                    "SELECT p FROM Project p JOIN p.users u WHERE u.id = :userId", Project.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur findByUserId projets: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public boolean assignUserToProject(Integer projectId, String registrationNumber) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Project project = session.find(Project.class, projectId);
            User user = session.createQuery("FROM User u WHERE u.matricule = :reg", User.class)
                    .setParameter("reg", registrationNumber)
                    .uniqueResult();

            if (project != null && user != null) {
                // S'assurer que l'ID est présent
                if (user.getId() == null) {
                    session.persist(user); // Persist si jamais l'objet est "transient"
                }

                project.getUsers().add(user);
                user.getProjects().add(project);

                session.merge(project); // merge le projet et les relations
                transaction.commit();
                System.out.println("[SUCCESS][DAO] Utilisateur " + registrationNumber + " assigné au projet ID=" + projectId);
                return true;
            } else {
                if (transaction != null) transaction.rollback();
                System.err.println("[INFO][DAO] Projet ou utilisateur introuvable.");
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[ERROR][DAO] Erreur assignation utilisateur à un projet : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public boolean removeUserFromProject(Integer projectId, String registrationNumber) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Project project = session.find(Project.class, projectId);
            User user = session.createQuery(
                            "FROM User u WHERE u.matricule = :reg", User.class)
                    .setParameter("reg", registrationNumber)
                    .uniqueResult();

            if (project != null && user != null) {
                project.getUsers().remove(user);
                user.getProjects().remove(project);
                session.merge(project);
                session.merge(user);
                transaction.commit();
                System.out.println("[SUCCESS][DAO] Utilisateur " + registrationNumber + " retiré du projet ID=" + projectId);
                return true;
            } else {
                System.err.println("[INFO][DAO] Projet ou utilisateur introuvable.");
                if (transaction != null) transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("[ERROR][DAO] Erreur suppression utilisateur dans un projet : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
