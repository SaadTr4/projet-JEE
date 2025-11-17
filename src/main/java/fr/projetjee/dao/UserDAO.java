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

/**
 * DAO spécialisé pour User — AVEC RECHERCHE MULTICRITÈRE CORRIGÉE
 */
public class UserDAO {

    public User save(User utilisateur) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(utilisateur);
            tx.commit();
            System.out.println("✅ Utilisateur créé: ID=" + utilisateur.getId() + ", matricule=" + utilisateur.getMatricule());
            return utilisateur;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }

    public User update(User utilisateur) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User merged = (User) session.merge(utilisateur);
            tx.commit();
            System.out.println("✅ Utilisateur mis à jour: ID=" + merged.getId());
            return merged;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User u = session.find(User.class, id);
            if (u != null) {
                session.remove(u);
                tx.commit();
                System.out.println("✅ Utilisateur supprimé ID=" + id);
                return true;
            } else {
                tx.commit();
                return false;
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public Optional<User> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.find(User.class, id));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> q = session.createQuery("FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email);
            return Optional.ofNullable(q.uniqueResult());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<User> findByMatricule(String matricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> q = session.createQuery("FROM User u WHERE u.matricule = :m", User.class);
            q.setParameter("m", matricule);
            return Optional.ofNullable(q.uniqueResult());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Récupère tous les users en fetchant les relations critiques pour éviter LazyInitializationException.
     */
    public List<User> findAllWithFetch() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> users = session.createQuery(
                    "SELECT DISTINCT u FROM User u " +
                            "LEFT JOIN FETCH u.department d " +
                            "LEFT JOIN FETCH u.position pos",
                    User.class
            ).list();

            System.out.println("✅ findAllWithFetch() - Trouvé " + users.size() + " employés");
            return users;
        } catch (Exception e) {
            System.err.println("❌ Erreur findAllWithFetch: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Recherche multi-critères CORRIGÉE avec paramètres nommés
     */
    public List<User> search(Integer deptId,
                             Integer posId,
                             Integer projectId,
                             Role role,
                             Grade grade,
                             String nameOrMatricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            StringBuilder hql = new StringBuilder(
                    "SELECT DISTINCT u FROM User u " +
                            "LEFT JOIN FETCH u.department d " +
                            "LEFT JOIN FETCH u.position pos " +
                            "WHERE 1=1"
            );

            // Construction dynamique avec paramètres NOMMÉS
            if (deptId != null) {
                hql.append(" AND u.department.id = :deptId");
            }
            if (posId != null) {
                hql.append(" AND u.position.id = :posId");
            }
            if (projectId != null) {
                hql.append(" AND EXISTS (SELECT pr FROM u.projects pr WHERE pr.id = :projectId)");
            }
            if (role != null) {
                hql.append(" AND u.role = :role");
            }
            if (grade != null) {
                hql.append(" AND u.grade = :grade");
            }
            if (nameOrMatricule != null && !nameOrMatricule.isBlank()) {
                hql.append(" AND (LOWER(u.firstName) LIKE LOWER(:searchText) " +
                        "OR LOWER(u.lastName) LIKE LOWER(:searchText) " +
                        "OR LOWER(u.matricule) LIKE LOWER(:searchText))");
            }

            Query<User> q = session.createQuery(hql.toString(), User.class);

            // Binding des paramètres nommés
            if (deptId != null) {
                q.setParameter("deptId", deptId);
            }
            if (posId != null) {
                q.setParameter("posId", posId);
            }
            if (projectId != null) {
                q.setParameter("projectId", projectId);
            }
            if (role != null) {
                q.setParameter("role", role);
            }
            if (grade != null) {
                q.setParameter("grade", grade);
            }
            if (nameOrMatricule != null && !nameOrMatricule.isBlank()) {
                q.setParameter("searchText", "%" + nameOrMatricule + "%");
            }

            List<User> results = q.list();

            System.out.println("🔍 RECHERCHE - Critères:");
            System.out.println("   - Département ID: " + deptId);
            System.out.println("   - Poste ID: " + posId);
            System.out.println("   - Rôle: " + role);
            System.out.println("   - Grade: " + grade);
            System.out.println("   - Texte: " + nameOrMatricule);
            System.out.println("   → Résultats: " + results.size() + " employé(s)");

            return results;

        } catch (Exception e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery("SELECT COUNT(u) FROM User u", Long.class);
            Long count = q.uniqueResult();
            System.out.println("📊 Nombre total d'employés: " + count);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Génération automatique matricule format EMP-0001
     */
    public String generateMatricule() {
        long count = count() + 1;
        String matricule = String.format("EMP-%04d", count);
        System.out.println("🏷️ Matricule généré: " + matricule);
        return matricule;
    }

    public List<User> findByGrade(Grade grade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> users = session.createQuery(
                            "SELECT DISTINCT u FROM User u " +
                                    "LEFT JOIN FETCH u.department " +
                                    "LEFT JOIN FETCH u.position " +
                                    "WHERE u.grade = :g",
                            User.class
                    ).setParameter("g", grade)
                    .list();

            System.out.println("🔍 findByGrade(" + grade + ") - Trouvé " + users.size() + " employés");
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}