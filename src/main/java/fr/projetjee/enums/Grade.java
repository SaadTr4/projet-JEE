package fr.projetjee.enums;


public enum Grade {
    JUNIOR("Junior"),
    SENIOR("Senior"),
    EXPERT("Expert");
    
    private final String displayName;
    
    Grade(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
