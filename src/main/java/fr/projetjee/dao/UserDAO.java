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
public class UserDAO extends GenericDAO<User, Integer> {

    public UserDAO() {
        super(User.class);
    }


    public Optional<User> findByMatricule(String matricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> q = session.createQuery("FROM User u WHERE u.matricule = :m", User.class);
            q.setParameter("m", matricule);
            return Optional.ofNullable(q.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur findByLastName: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur findAll: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> q = session.createQuery("FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email);
            return Optional.ofNullable(q.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur findByEmail: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("[ERROR][DAO] Erreur findByLastName: " + e.getMessage());
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
            System.err.println("[ERROR][DAO] Erreur findByFirstName: " + e.getMessage());
            return new ArrayList<>();
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

    public List<User> findByRole(Role role) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.role = :role", User.class);
            query.setParameter("role", role);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur findByRole: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<User> findByContractType(String contractType) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.contractType = :contractType", User.class);
            query.setParameter("contractType", contractType);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur findByContractType: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean isUserProjectManager(Role role, Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.role = :role AND u.id = :id",
                    User.class
            );
            query.setParameter("role", role);
            query.setParameter("id", id);
            return query.uniqueResult() != null;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur isUserProjectManager: " + e.getMessage());
            return false;
        }
    }

    
   public List<User> findByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User u WHERE u.department.id = :deptId", User.class);
            query.setParameter("deptId", departmentId);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur findByDepartment: " + e.getMessage());
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
            System.err.println("[ERROR][DAO]Erreur recherche: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("[ERROR][DAO] Erreur findByProject: " + e.getMessage());
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
            System.err.println("[ERROR][DAO] Erreur findByPosition: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<User> findByEmailOrMatricule(String login) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.email = :login OR u.matricule = :login", User.class);
            query.setParameter("login", login);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur findByEmailOrMatricule: " + e.getMessage());
            return Optional.empty();
        }
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery("SELECT COUNT(u) FROM User u", Long.class);
            Long count = q.uniqueResult();
            System.out.println("📊 Nombre total d'employés: " + count);
            return count;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur count: " + e.getMessage());
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
            System.err.println("[ERROR][DAO] Erreur countByDepartment: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public long countByGrade(Grade grade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.grade = :grade", Long.class);
            query.setParameter("grade", grade);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur countByGrade: " + e.getMessage());
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
            System.err.println("[ERROR][DAO] Erreur countByDepartment: " + e.getMessage());
            return 0;
        }
    }
    //  Vérifier existence par ID
    public boolean exists(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User utilisateur = session.find(User.class, id);
            return utilisateur != null;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur exists: " + e.getMessage());
            return false;
        }
    }

}