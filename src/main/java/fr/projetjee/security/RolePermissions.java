package fr.projetjee.security;

import fr.projetjee.dao.DepartmentDAO;
import fr.projetjee.dao.ProjectDAO;
import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.Action;
import fr.projetjee.enums.Role;
import fr.projetjee.model.Department;
import fr.projetjee.model.Project;
import fr.projetjee.model.User;

import java.util.*;

public class RolePermissions {
    private static final Map<Role, Set<Action>> permissions = new HashMap<>();

    static {
        // ADMINISTRATEUR : tous les droits
        permissions.put(Role.ADMINISTRATEUR, EnumSet.allOf(Action.class));

        // CHEF_DEPARTEMENT : gestion complète de son département
        permissions.put(Role.CHEF_DEPARTEMENT, EnumSet.of(
                // Projets
                Action.READ_PROJECT, Action.CREATE_PROJECT, Action.UPDATE_PROJECT,
                Action.DELETE_PROJECT, Action.FILTER_PROJECT,
                // Bulletins
                Action.READ_PAYSLIP, Action.EXPORT_PAYSLIP, Action.FILTER_PAYSLIP,
                // Utilisateurs (lecture + modification de son département)
                Action.READ_USER, Action.FILTER_USER
        ));

        // CHEF_PROJET : consultation et gestion de projets
        permissions.put(Role.CHEF_PROJET, EnumSet.of(
                Action.READ_PROJECT, Action.UPDATE_PROJECT, Action.DELETE_PROJECT,
                Action.READ_PAYSLIP, Action.EXPORT_PAYSLIP, Action.FILTER_PAYSLIP,
                Action.READ_USER, Action.FILTER_USER
        ));

        // EMPLOYE : consultation uniquement
        permissions.put(Role.EMPLOYE, EnumSet.of(
                Action.READ_PROJECT,
                Action.READ_PAYSLIP, Action.EXPORT_PAYSLIP, Action.FILTER_PAYSLIP
        ));
    }

    // ==================== VÉRIFICATION DE BASE ====================

    public static boolean hasPermission(Role role, Action action) {
        return permissions.getOrDefault(role, Collections.emptySet()).contains(action);
    }

    // ==================== IDENTIFICATIONS DE RÔLES ====================

    public static boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMINISTRATEUR;
    }

    public static boolean isRH(User user) {
        return user != null &&
                user.getDepartment() != null &&
                "RH".equalsIgnoreCase(user.getDepartment().getCode());
    }

    public static boolean isDepartmentHeadRH(User user) {
        return user != null &&
                user.getRole() == Role.CHEF_DEPARTEMENT &&
                isRH(user);
    }

    public static boolean isEmployeRH(User user) {
        return user != null &&
                user.getRole() == Role.EMPLOYE &&
                isRH(user);
    }

    public static boolean isDepartmentHead(User user) {
        return user != null && user.getRole() == Role.CHEF_DEPARTEMENT;
    }

    // ==================== PERMISSIONS MÉTIER UTILISATEURS ====================

    /**
     * Can view all users (admin or departemnt HR head)
     */
    public static boolean canViewAllUsers(User user) {
        return isAdmin(user) || isDepartmentHeadRH(user);
    }

    /**
     * Can access user list
     */
    public static boolean canAccessUserList(User user) {
        if (user == null) return false;

        // Employés normaux : INTERDIT
        if (user.getRole() == Role.EMPLOYE && !isRH(user)) {
            return false;
        }

        // RH ou permissions READ_USER
        return isRH(user) || hasPermission(user.getRole(), Action.READ_USER);
    }

    /**
     * Can create a new user (critical operation, only admin or department HR head)
     */
    public static boolean canCreateUser(User user) {
        return isAdmin(user) ||
                isDepartmentHeadRH(user);
    }

    /**
     * Can delete a user (critcal operation, only admin or department HR head)
     */
    public static boolean canDeleteUser(User user) {
        return isAdmin(user) ||
                isDepartmentHeadRH(user);
    }
    public static boolean canDeleteUserWithTarget(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) return false;

        // Admin : peut tout supprimer
        if (isAdmin(currentUser)) return true;

        // Chef de département RH : peut supprimer tout sauf les admins
        if (isDepartmentHeadRH(currentUser) && !isAdmin(targetUser)) return true;

        return false;
    }


    /**
     * Peut assigner le rôle ADMINISTRATEUR
     */
    public static boolean canAssignAdminRole(User currentUser) {
        return isAdmin(currentUser);
    }

    /**
     * Peut assigner le département RH
     */
    public static boolean canAssignRHDepartment(User currentUser) {
        return isAdmin(currentUser) || isDepartmentHeadRH(currentUser);
    }
    /**
     * Vérifie si l'utilisateur tente de se modifier lui-même
     */
    public static boolean isSelfEdit(User currentUser, User targetUser) {
        return currentUser != null && targetUser != null &&
                currentUser.getId().equals(targetUser.getId());
    }

    /**
     * Can view private information (nom, prénom, email, etc.)
     */
    public static boolean canViewPrivateInfo(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) return false;

        // Admin : tout voir
        if (isAdmin(currentUser)) return true;

        // RH : voir tout sauf autres RH et admin
        if (isRH(currentUser) && !isRH(targetUser) && !isAdmin(targetUser)) return true;

        // Chef département : voir son département uniquement
        if (isDepartmentHead(currentUser) &&
                currentUser.getDepartment() != null &&
                currentUser.getDepartment().equals(targetUser.getDepartment())) {
            return true;
        }

        return false;
    }

    /**
     * Can modify private information (nom, prénom, email, etc.)
     */
    public static boolean canUpdatePrivateInfo(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) return false;

        // Chef département ne peut pas modifier ses propres infos privées
        if (isDepartmentHead(currentUser) && isSelfEdit(currentUser, targetUser)) {
            return false;
        }

        // Admin : tout modifier
        if (isAdmin(currentUser)) return true;

        // Chef département RH : tout modifier sauf admin
        if (isDepartmentHeadRH(currentUser) && !isAdmin(targetUser)) return true;

        // Employé RH : modifier tout sauf RH
        if (isEmployeRH(currentUser) && !isRH(targetUser)) return true;

        return false;
    }

    /**
     * Can modify public information (grade, poste)
     */
    public static boolean canUpdatePublicInfo(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) return false;

        // Admin : tout modifier
        if (isAdmin(currentUser)) return true;

        // RH : modifier tout sauf RH
        if (isRH(currentUser) && !isRH(targetUser)) return true;

        // Chef département : modifier son département
        if (isDepartmentHead(currentUser) &&
                currentUser.getDepartment() != null &&
                currentUser.getDepartment().equals(targetUser.getDepartment())) {
            return true;
        }

        return false;
    }

    public static boolean canUpdateSalary(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) return false;

        // Admin peut modifier le salaire de n'importe qui sauf son propre salaire
        if (isAdmin(currentUser) && !isSelfEdit(currentUser, targetUser)) return true;

        // Chef département RH peut modifier le salaire des employés RH, mais pas Admin et pas son propre salaire
        if (isDepartmentHeadRH(currentUser) && !isAdmin(targetUser) && !isSelfEdit(currentUser, targetUser)) return true;

        return false;
    }

    public static String validateDepartmentAssignment(User currentUser, Integer requestedDeptId, DepartmentDAO departmentDAO) {

        try {
            Optional<Department> deptOpt = departmentDAO.findById(requestedDeptId);

            if (deptOpt.isPresent() && "RH".equalsIgnoreCase(deptOpt.get().getCode())) {
                if (!canAssignRHDepartment(currentUser)) {
                    return "Vous n'avez pas la permission d'assigner le département RH.";
                }
            }
        } catch (NumberFormatException e) {
            return "Département invalide.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la validation du département.";
        }
        return null; // Validation réussie
    }

    /**
     * Valide l'assignation d'un rôle avec toutes les règles métier
     * @return Message d'erreur si invalide, null si valide
     */
    public static String validateRoleAssignment(User currentUser, User targetUser,
                                                Role requestedRole, String deptStr,
                                                UserDAO userDAO, DepartmentDAO departmentDAO) {

        // Vérification : Assignation du rôle ADMINISTRATEUR
        if (requestedRole == Role.ADMINISTRATEUR && !canAssignAdminRole(currentUser)) {
            return "Vous n'avez pas la permission d'assigner le rôle Administrateur.";
        }

        // Vérification : Assignation du rôle CHEF_DEPARTEMENT (unicité)
        if (requestedRole == Role.CHEF_DEPARTEMENT && deptStr != null && !deptStr.isBlank()) {
            try {
                Integer requestedDeptId = Integer.parseInt(deptStr);
                Optional<User> existingChef = userDAO.findHeadByDepartmentId(requestedDeptId);

                if (existingChef.isPresent() && !existingChef.get().getId().equals(targetUser.getId())) {
                    User chef = existingChef.get();
                    String deptName = departmentDAO.findById(requestedDeptId).map(d -> d.getName()).orElse("ce département");
                    return "Ce département a déja un " + chef.getRole().getDisplayName() + "! Allez dans la section Départements si vous souhaitez assigner un nouveau " + chef.getRole().getDisplayName() +".";
                }
            } catch (NumberFormatException e) {
                return "Département invalide.";
            } catch (Exception e) {
                e.printStackTrace();
                return "Erreur lors de la vérification du chef de département.";
            }
        }

        return null; // Validation réussie
    }

    /**
     * Vérifie si un utilisateur peut accéder aux détails d'un projet.
     */
    public static boolean userCanAccessProject(User user, Project project) {

        Role role = user.getRole();

        boolean isRH = RolePermissions.isRH(user);

        // Admin / RH / Chef de département → accès total
        if (RolePermissions.isAdmin(user) ||
                RolePermissions.isRH(user) ||
                role == Role.CHEF_DEPARTEMENT) {
            return true;
        }

        // Chef de projet : accès si c'est SON projet
        if (role == Role.CHEF_PROJET) {
            return project.getProjectManager().getId().equals(user.getId());
        }

        // Employé et chef projet normal → accès seulement si assigné
        boolean assigned = project.getUsers()
                .stream()
                .anyMatch(u -> u.getId().equals(user.getId()));
        return assigned;
    }

    /**
     * Vérifie si un employé peut être assigné à un projet (pas RH, pas Admin)
     */
    public static boolean canBeAssignedToProject(User user) {
        if (user == null) return false;

        // Exclure les administrateurs
        if (isAdmin(user)) return false;

        // Exclure les employés RH
        if (isRH(user)) return false;

        return true;
    }

    /**
     * Vérifie si l'utilisateur peut gérer les membres d'un projet (assigner/retirer)
     */
    public static boolean canManageProjectMembers(User currentUser, Project project) {
        if (currentUser == null || project == null) return false;

        // Admin : toujours autorisé
        if (isAdmin(currentUser)) return true;

        // Employé RH : toujours autorisé
        if (isEmployeRH(currentUser)) return true;

        // Chef de département : toujours autorisé
        if (isDepartmentHead(currentUser)) return true;

        // Chef de projet : seulement si c'est SON projet
        if (currentUser.getRole() == Role.CHEF_PROJET) {
            return project.getProjectManager() != null &&
                    project.getProjectManager().getId().equals(currentUser.getId());
        }

        return false;
    }

    /**
     * Vérifie si l'utilisateur peut voir la colonne matricule dans les détails projet
     */
    public static boolean canViewMatriculeColumn(User user) {
        if (user == null) return false;

        // Employé classique ne peut pas voir les matricules
        if (user.getRole() == Role.EMPLOYE && !isRH(user)) {
            return false;
        }

        return true;
    }

}