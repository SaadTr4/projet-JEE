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

public class UserDAO extends GenericDAO<User, Integer> {

    public UserDAO() {
        super(User.class);
    }

    // ==================== RECHERCHES PAR CHAMP UNIQUE ====================

    public Optional<User> findByMatricule(String matricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> q = session.createQuery("FROM User u WHERE u.matricule = :m", User.class);
            q.setParameter("m", matricule);
            return Optional.ofNullable(q.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] findByMatricule: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> q = session.createQuery("FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email);
            return Optional.ofNullable(q.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] findByEmail: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<User> findByEmailOrMatricule(String login) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.email = :login OR u.matricule = :login", User.class);
            query.setParameter("login", login);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] findByEmailOrMatricule: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ==================== RECHERCHES PAR NOM ====================

    public List<User> findByLastName(String lastName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:lastName)", User.class);
            query.setParameter("lastName", "%" + lastName + "%");
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] findByLastName: " + e.getMessage());
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
            System.err.println("[ERROR][DAO] findByFirstName: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== LISTES COMPLÈTES ====================

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] findAll: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<User> findAllWithFetch() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> users = session.createQuery(
                    "SELECT DISTINCT u FROM User u " +
                            "LEFT JOIN FETCH u.department d " +
                            "LEFT JOIN FETCH u.position pos",
                    User.class
            ).list();
            System.out.println(" findAllWithFetch() - Trouvé " + users.size() + " employés");
            return users;
        } catch (Exception e) {
            System.err.println(" Erreur findAllWithFetch: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== RECHERCHES PAR RÔLE/GRADE ====================

    public List<User> findByRole(Role role) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.role = :role", User.class);
            query.setParameter("role", role);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] findByRole: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<User> findByGrade(Grade grade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> users = session.createQuery(
                    "SELECT DISTINCT u FROM User u " +
                            "LEFT JOIN FETCH u.department " +
                            "LEFT JOIN FETCH u.position " +
                            "WHERE u.grade = :g",
                    User.class
            ).setParameter("g", grade).list();
            System.out.println(" findByGrade(" + grade + ") - Trouvé " + users.size() + " employés");
            return users;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] findByGrade: " + e.getMessage());
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
            System.err.println("[ERROR][DAO] findByContractType: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== RECHERCHES PAR DÉPARTEMENT/POSTE ====================

    public List<User> findByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.department.id = :deptId", User.class);
            query.setParameter("deptId", departmentId);
            return query.list();
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] findByDepartment: " + e.getMessage());
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
            System.err.println("[ERROR][DAO] findByPosition: " + e.getMessage());
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
            System.err.println("[ERROR][DAO] findByProject: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== RECHERCHE MULTICRITÈRE ====================

    public List<User> search(Integer deptId, Integer posId, Integer projectId, Role role, Grade grade, String nameOrMatricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder(
                    "SELECT DISTINCT u FROM User u " +
                            "LEFT JOIN FETCH u.department d " +
                            "LEFT JOIN FETCH u.position pos " +
                            "WHERE 1=1"
            );

            if (deptId != null) hql.append(" AND u.department.id = :deptId");
            if (posId != null) hql.append(" AND u.position.id = :posId");
            if (projectId != null) hql.append(" AND EXISTS (SELECT pr FROM u.projects pr WHERE pr.id = :projectId)");
            if (role != null) hql.append(" AND u.role = :role");
            if (grade != null) hql.append(" AND u.grade = :grade");
            if (nameOrMatricule != null && !nameOrMatricule.isBlank()) {
                hql.append(" AND (LOWER(u.firstName) LIKE LOWER(:searchText) " +
                        "OR LOWER(u.lastName) LIKE LOWER(:searchText) " +
                        "OR LOWER(u.matricule) LIKE LOWER(:searchText))");
            }

            Query<User> q = session.createQuery(hql.toString(), User.class);

            if (deptId != null) q.setParameter("deptId", deptId);
            if (posId != null) q.setParameter("posId", posId);
            if (projectId != null) q.setParameter("projectId", projectId);
            if (role != null) q.setParameter("role", role);
            if (grade != null) q.setParameter("grade", grade);
            if (nameOrMatricule != null && !nameOrMatricule.isBlank()) {
                q.setParameter("searchText", "%" + nameOrMatricule + "%");
            }

            List<User> results = q.list();
            System.out.println(" RECHERCHE - " + results.size() + " résultat(s)");
            return results;

        } catch (Exception e) {
            System.err.println("[ERROR][DAO] Erreur recherche: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ==================== VÉRIFICATIONS ====================

    public boolean isUserProjectManager(Role role, Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.role = :role AND u.id = :id", User.class);
            query.setParameter("role", role);
            query.setParameter("id", id);
            return query.uniqueResult() != null;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] isUserProjectManager: " + e.getMessage());
            return false;
        }
    }

    public boolean exists(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User utilisateur = session.find(User.class, id);
            return utilisateur != null;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] exists: " + e.getMessage());
            return false;
        }
    }

    // ==================== COMPTAGES ====================

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery("SELECT COUNT(u) FROM User u", Long.class);
            Long count = q.uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] count: " + e.getMessage());
            return 0;
        }
    }

    public long countByGrade(Grade grade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.grade = :grade", Long.class);
            query.setParameter("grade", grade);
            Long count = query.uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] countByGrade: " + e.getMessage());
            return 0;
        }
    }

    public long countByDepartment(Integer departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.department.id = :deptId", Long.class);
            query.setParameter("deptId", departmentId);
            Long count = query.uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("[ERROR][DAO] countByDepartment: " + e.getMessage());
            return 0;
        }
    }

    // ==================== UTILITAIRES ====================

    public String generateMatricule() {
        long count = count() + 1;
        String matricule = String.format("EMP%03d", count);  //
        System.out.println("🏷 Matricule généré: " + matricule);
        return matricule;
    }

    // ==================== SURCHARGE SAVE/UPDATE POUR CORRIGER BUG SESSION ====================

    /**
     * Surcharge de save() pour corriger le bug de session fermée lors du rollback
     * Gestion manuelle de la session pour éviter que try-with-resources ne ferme la session
     * avant le rollback dans le catch
     */
    @Override
    public User save(User entity) {
        if(entity == null) {
            throw new IllegalArgumentException("L'entité à sauvegarder ne peut pas être null");
        }

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.persist(entity);

            transaction.commit();
            System.out.println("[SUCCESS][DAO] User sauvegardé: " + entity.getFullName());
            return entity;

        } catch (Exception e) {
            //  Rollback AVANT fermeture de session
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("[ERROR][DAO] Erreur rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("[ERROR][DAO] sauvegarde User: " + e.getMessage());
            e.printStackTrace();
            return null;

        } finally {
            //  Fermeture manuelle de session
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    System.err.println("[ERROR][DAO] Erreur fermeture session: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Surcharge de update() pour corriger le bug de session fermée lors du rollback
     * Gestion manuelle de la session pour éviter que try-with-resources ne ferme la session
     * avant le rollback dans le catch
     */
    @Override
    public User update(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité à mettre à jour ne peut pas être null");
        }

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User merged = (User) session.merge(entity);

            transaction.commit();
            System.out.println("[SUCCESS][DAO] User mis à jour : " + merged.getFullName());
            return merged;

        } catch (Exception e) {
            //  Rollback AVANT fermeture de session
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("[ERROR][DAO] Erreur rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("[ERROR][DAO] Erreur update User: " + e.getMessage());
            e.printStackTrace();
            return null;

        } finally {
            //  Fermeture manuelle de session
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    System.err.println("[ERROR][DAO] Erreur fermeture session: " + closeEx.getMessage());
                }
            }
        }
    }
}