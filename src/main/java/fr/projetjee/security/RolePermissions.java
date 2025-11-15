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
                Action.FILTER_PROJECT, Action.READ_PAYSLIP, Action.EXPORT_PAYSLIP, Action.FILTER_PAYSLIP
        ));
        permissions.put(Role.CHEF_PROJET, EnumSet.of(Action.READ_PROJECT, Action.UPDATE_PROJECT, Action.DELETE_PROJECT, Action.READ_PAYSLIP));
        permissions.put(Role.EMPLOYE, EnumSet.of(Action.READ_PROJECT, Action.READ_PAYSLIP));
    }

    public static boolean hasPermission(Role role, Action action) {
        return permissions.getOrDefault(role, Collections.emptySet()).contains(action);
    }
}
