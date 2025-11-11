package fr.projetjee.dto;

import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.model.User;
import java.util.Base64;

/**
 * DTO pour User avec gestion des images en Base64
 */
public class UserDTO {

    private Integer id;
    private String matricule;
    private String lastName;
    private String firstName;
    private String email;
    private String phone;
    private String address;
    private String imageBase64;
    private Grade grade;
    private Role role;

    // Relations
    private Integer departmentId;
    private String departmentName;
    private Integer positionId;
    private String positionName;

    // ========================================
    // CONSTRUCTEURS
    // ========================================

    public UserDTO() {}

    /**
     * Convertit User → UserDTO
     */
    public static UserDTO fromEntity(User user) {
        return fromEntity(user, true);
    }

    /**
     * Convertit User → UserDTO avec option image
     */
    public static UserDTO fromEntity(User user, boolean includeImage) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setMatricule(user.getMatricule());
        dto.setLastName(user.getLastName());
        dto.setFirstName(user.getFirstName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setGrade(user.getGrade());
        dto.setRole(user.getRole());

        // Convertir image byte[] → Base64
        if (includeImage && user.getImage() != null && user.getImage().length > 0) {
            dto.setImageBase64(Base64.getEncoder().encodeToString(user.getImage()));
        }

        // Relations
        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }

        if (user.getPosition() != null) {
            dto.setPositionId(user.getPosition().getId());
            dto.setPositionName(user.getPosition().getName());
        }

        return dto;
    }

    /**
     * Convertit UserDTO → User
     */
    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setMatricule(this.matricule);
        user.setLastName(this.lastName);
        user.setFirstName(this.firstName);
        user.setEmail(this.email);
        user.setPhone(this.phone);
        user.setAddress(this.address);
        user.setGrade(this.grade);
        user.setRole(this.role);

        // Convertir Base64 → byte[]
        if (this.imageBase64 != null && !this.imageBase64.isEmpty()) {
            try {
                user.setImage(Base64.getDecoder().decode(this.imageBase64));
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Image Base64 invalide: " + e.getMessage());
            }
        }

        return user;
    }

    /**
     * Met à jour une entité existante
     */
    public void updateEntity(User user) {
        if (this.matricule != null) user.setMatricule(this.matricule);
        if (this.lastName != null) user.setLastName(this.lastName);
        if (this.firstName != null) user.setFirstName(this.firstName);
        if (this.email != null) user.setEmail(this.email);
        if (this.phone != null) user.setPhone(this.phone);
        if (this.address != null) user.setAddress(this.address);
        if (this.grade != null) user.setGrade(this.grade);
        if (this.role != null) user.setRole(this.role);

        // Mettre à jour l'image
        if (this.imageBase64 != null && !this.imageBase64.isEmpty()) {
            try {
                user.setImage(Base64.getDecoder().decode(this.imageBase64));
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Image Base64 invalide");
            }
        }
    }

    // ========================================
    // GETTERS ET SETTERS
    // ========================================

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Integer getPositionId() { return positionId; }
    public void setPositionId(Integer positionId) { this.positionId = positionId; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
}
