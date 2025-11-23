package fr.projetjee.security;

import fr.projetjee.enums.Action;
import fr.projetjee.enums.Role;

import java.util.*;

public class RolePermissions {
    private static final Map<Role, Set<Action>> permissions = new HashMap<>();

    static {
        permissions.put(Role.ADMINISTRATEUR, EnumSet.allOf(Action.class));
        permissions.put(Role.CHEF_DEPARTEMENT, EnumSet.of(
                Action.READ_PROJECT, Action.CREATE_PROJECT, Action.UPDATE_PROJECT, Action.DELETE_PROJECT,
                Action.FILTER_PROJECT, Action.READ_PAYSLIP, Action.EXPORT_PAYSLIP, Action.FILTER_PAYSLIP,
                //  Employés
                Action.READ_USER, Action.CREATE_USER, Action.UPDATE_USER,
                Action.DELETE_USER, Action.FILTER_USER
        ));
        permissions.put(Role.CHEF_PROJET, EnumSet.of(Action.READ_PROJECT, Action.UPDATE_PROJECT, Action.DELETE_PROJECT, Action.READ_PAYSLIP, Action.EXPORT_PAYSLIP, Action.FILTER_PAYSLIP,
                //  Employés (LECTURE SEULE)
                Action.READ_USER, Action.FILTER_USER));

        permissions.put(Role.EMPLOYE, EnumSet.of(Action.READ_PROJECT, Action.READ_PAYSLIP, Action.READ_PAYSLIP, Action.EXPORT_PAYSLIP, Action.FILTER_PAYSLIP,
                // cv Employés (LECTURE SEULE)
                Action.READ_USER, Action.FILTER_USER));
    }

    public static boolean hasPermission(Role role, Action action) {
        return permissions.getOrDefault(role, Collections.emptySet()).contains(action);
    }
}
