package fr.projetjee.models;

public class Payslip {
    private int id;
    private String employeNom;
    private double salaireBase;
    private double prime;
    private double deduction;
    private double total;

    public Payslip(int id, String employeNom, double salaireBase, double prime, double deduction) {
        this.id = id;
        this.employeNom = employeNom;
        this.salaireBase = salaireBase;
        this.prime = prime;
        this.deduction = deduction;
        this.total = salaireBase + prime - deduction;
    }

    public int getId() { return id; }
    public String getEmployeNom() { return employeNom; }
    public double getSalaireBase() { return salaireBase; }
    public double getPrime() { return prime; }
    public double getDeduction() { return deduction; }
    public double getTotal() { return total; }
}
