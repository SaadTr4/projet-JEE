package fr.projetjee.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payslip")
public class Payslip implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;
    @Column(name = "generation_date", nullable = false)
    private LocalDate generationDate;

    @Column(name = "month", nullable = false)
    private Integer month; // 1 = janvier, 12 = décembre

    @Column(name = "year", nullable = false)
    private Integer year;

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
    public Payslip(Integer year, Integer month, BigDecimal baseSalary, BigDecimal bonuses, BigDecimal deductions, User user) {
        this.year = year;
        this.month = month;
        this.baseSalary = baseSalary;
        this.bonuses = bonuses != null ? bonuses : BigDecimal.ZERO;
        this.deductions = deductions != null ? deductions : BigDecimal.ZERO;
        this.user = user;
        this.generationDate = LocalDate.now();
        this.calculateNetPay();
    }

    // ===============================
    // Getters & Setters
    // ===============================
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDate generationDate) { this.generationDate = generationDate; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

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

    public void addBonus(BigDecimal amount) {
        if (amount != null) {
            this.bonuses = this.bonuses.add(amount);
            calculateNetPay();
        }
    }

    // Delete bonus (reset to zero)
    public void clearBonus() {
        this.bonuses = BigDecimal.ZERO;
        calculateNetPay();
    }
    public void addDeduction(BigDecimal amount) {
        if (amount != null) {
            this.deductions = this.deductions.add(amount);
            calculateNetPay();
        }
    }

    // Delete deduction (reset to zero)
    public void clearDeduction() {
        this.deductions = BigDecimal.ZERO;
        calculateNetPay();
    }

    // Update base salary
    public void updateBaseSalary(BigDecimal amount) {
        if (amount != null) {
            this.baseSalary = amount;
            calculateNetPay();
        }
    }


    @Override
    public String toString() {
        return "Payslip [\n" +
                "                id = " + id + ",\n" +
                "    generationDate = " + generationDate + ",\n" +
                "             month = " + month + ",\n" +
                "              year = " + year + ",\n" +
                "        baseSalary = " + baseSalary + ",\n" +
                "           bonuses = " + bonuses + ",\n" +
                "        deductions = " + deductions + ",\n" +
                "            netPay = " + netPay + ",\n" +
                "              user = " + (user != null ? user.getId() : null) + "\n" +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payslip)) return false;
        Payslip other = (Payslip) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
