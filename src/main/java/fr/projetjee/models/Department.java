package fr.projetjee.models;

public class Department {
    private int id;
    private String nom;
    private String chef;
    private int nbEmployes;

    public Department(int id, String nom, String chef, int nbEmployes) {
        this.id = id;
        this.nom = nom;
        this.chef = chef;
        this.nbEmployes = nbEmployes;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getChef() { return chef; }
    public int getNbEmployes() { return nbEmployes; }

    public void setNom(String nom) { this.nom = nom; }
    public void setChef(String chef) { this.chef = chef; }
    public void setNbEmployes(int nbEmployes) { this.nbEmployes = nbEmployes; }
}
