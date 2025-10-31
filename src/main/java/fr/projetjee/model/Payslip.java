package fr.projetjee.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payslip")
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Temporal(TemporalType.DATE)
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "base_salary", precision = 10, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "bonuses", precision = 10, scale = 2)
    private BigDecimal bonuses;

    @Column(name = "deductions", precision = 10, scale = 2)
    private BigDecimal deductions;

    @Column(name = "net_pay", precision = 10, scale = 2)
    private BigDecimal netPay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_number", referencedColumnName = "registration_number")
    private User user;

    // ===============================
    // Constructors
    // ===============================
    public Payslip() {}
    public Payslip(LocalDate date, BigDecimal baseSalary, BigDecimal bonuses,
                   BigDecimal deductions, User user) {
        this.date = date;
        this.baseSalary = baseSalary;
        this.bonuses = bonuses != null ? bonuses : BigDecimal.ZERO;
        this.deductions = deductions != null ? deductions : BigDecimal.ZERO;
        this.user = user;
        this.calculateNetPay(); // ⚙️ auto-calcul du net
    }

    // ===============================
    // Getters & Setters
    // ===============================
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
    public BigDecimal getBonuses() { return bonuses; }
    public void setBonuses(BigDecimal bonuses) { this.bonuses = bonuses; }
    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // ==============================
    // Utility Methods
    // ==============================
    public void calculateNetPay() {
        this.netPay = baseSalary
                .add(bonuses != null ? bonuses : BigDecimal.ZERO)
                .subtract(deductions != null ? deductions : BigDecimal.ZERO);
    }
    @Override
    public String toString() {
        return "Payslip{" +
                "id=" + id +
                ", date=" + date +
                ", baseSalary=" + baseSalary +
                ", bonuses=" + bonuses +
                ", deductions=" + deductions +
                ", netPay=" + netPay +
                '}';
    }
}
