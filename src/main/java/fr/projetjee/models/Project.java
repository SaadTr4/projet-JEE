package fr.projetjee.models;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private int id;
    private String nom;
    private String chefProjet;
    private String statut;
    private List<String> employesAffectes = new ArrayList<>();

    public Project(int id, String nom, String chefProjet, String statut) {
        this.id = id;
        this.nom = nom;
        this.chefProjet = chefProjet;
        this.statut = statut;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getChefProjet() { return chefProjet; }
    public String getStatut() { return statut; }
    public List<String> getEmployesAffectes() { return employesAffectes; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setChefProjet(String chefProjet) { this.chefProjet = chefProjet; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setEmployesAffectes(List<String> employesAffectes) { this.employesAffectes = employesAffectes; }
}
