package fr.projetjee.enums;


public enum Role {
    ADMINISTRATEUR("Administrateur"),
    CHEF_DEPARTEMENT("Chef de Département"),
    CHEF_PROJET("Chef de Projet"),
    EMPLOYE("Employé");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
