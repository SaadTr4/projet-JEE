package fr.projetjee.models;

public class Employee {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String departement;
    private String projet;
    private double salaire;

    public Employee(int id, String nom, String prenom, String email, String role,
                    String departement, String projet, double salaire) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.departement = departement;
        this.projet = projet;
        this.salaire = salaire;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDepartement() { return departement; }
    public String getProjet() { return projet; }
    public double getSalaire() { return salaire; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setDepartement(String departement) { this.departement = departement; }
    public void setProjet(String projet) { this.projet = projet; }
    public void setSalaire(double salaire) { this.salaire = salaire; }
}
