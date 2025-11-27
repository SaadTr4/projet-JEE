package fr.projetjee.model;

import fr.projetjee.enums.ContractType;
import jakarta.persistence.*;
import fr.projetjee.model.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Blob;
import java.util.HashSet;
import java.util.Set;

import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;

/**
 * Entité représentant un utilisateur/employé de l'entreprise
 */
@Entity
@Table(name = "user_account")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ✅ ID auto-généré comme clé primaire
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    //  Matricule comme identifiant métier unique
    @Column(name = "registration_number", unique = true, nullable = false, length = 20)
    private String matricule;
    
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.BINARY)  // ← Force BYTEA
    @Column(name = "image")
    private byte[] image;

    @Column(name = "address", length = 255)
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "grade", length = 20)
    private Grade grade;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 30)
    private ContractType contractType;

    @Column(name = "base_salary", precision = 10, scale = 2)
    private BigDecimal baseSalary;

    // Relation Many-to-One avec Department
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;
    
    // Relation Many-to-One avec Position
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id")
    private fr.projetjee.model.Position position;
    
    // Relation Many-to-Many avec Project
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_project",
        joinColumns = @JoinColumn(name = "user_id"),  // ✅ Changé de employee_number à user_id
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new HashSet<>();
    
    // Relation One-to-Many avec Payslip
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Payslip> payslips = new HashSet<>();
    
    // ========================================
    // CONSTRUCTEURS
    // ========================================
    
    public User() {
        this.baseSalary = BigDecimal.ZERO;
    }
    
    public User(String matricule, String lastName, String firstName, String email, ContractType contractType) {
        this.matricule = matricule;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.contractType = contractType;
        this.baseSalary = BigDecimal.ZERO;
    }
    
    // ========================================
    // GETTERS ET SETTERS
    // ========================================
    
    // ✅ Getter/Setter pour ID
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getMatricule() {
        return matricule;
    }
    
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Grade getGrade() {
        return grade;
    }
    
    public void setGrade(Grade grade) {
        this.grade = grade;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }

    public ContractType getContractType() { return contractType; }
    public void setContractType(ContractType contractType) { this.contractType = contractType; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
   public Department getDepartment() {
        return department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = position;
    }
    
    public Set<Project> getProjects() {
        return projects;
    }
    
    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }
    
    public Set<Payslip> getPayslips() {
        return payslips;
    }
    
    public void setPayslips(Set<Payslip> payslips) {
        this.payslips = payslips;
    }
    
    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // ========================================
    // MÉTHODES STANDARDS
    // ========================================

    @Override
    public String toString() {
        return "User [\n" +
                "               id  = " + id + ",\n" +
                "        matricule  = " + matricule + ",\n" +
                "         lastName  = " + lastName + ",\n" +
                "        firstName  = " + firstName + ",\n" +
                "            email  = " + email + ",\n" +
                "            phone  = " + phone + ",\n" +
                "          address  = " + address + ",\n" +
                "            grade  = " + grade + ",\n" +
                "             role  = " + role + "\n" +
                 "    contractType  = " + contractType + ",\n" +
                ']';

    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return id != null && id.equals(that.id);  // ✅ Comparaison par ID
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}