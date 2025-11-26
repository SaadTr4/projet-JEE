package fr.projetjee.enums;

public enum ContractType {
    PERMANENT_FULL_TIME("CDI"),
    PERMANENT_PART_TIME("CDI temps partiel"),
    FIXED_TERM_FULL_TIME("CDD"),
    FIXED_TERM_PART_TIME("CDD temps partiel"),
    TEMPORARY_AGENCY("Intérim"),
    INTERNSHIP("Stage"),
    APPRENTICESHIP("Alternance / Apprentissage"),
    FREELANCE_CONTRACTOR("Indépendant / Freelance"),
    ZERO_HOURS("Contrat zéro heure"),
    SEASONAL("Contrat saisonnier"),
    ON_CALL("Contrat d’astreinte"),
    VOLUNTARY_UNPAID("Bénévolat / Stage non rémunéré");

    private final String displayName;

    ContractType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

