package fr.projetjee.model;

import fr.projetjee.enums.Status;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "project")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "project_manager", length = 100)
    private String projectManager;
    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    // ==============================
    // Constructors
    // ==============================
    public Project() {
        this.status = Status.IN_PROGRESS; // Default status
    }
    public Project(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.IN_PROGRESS; // Default status
    }
    public Project(String name, String projectManager, Status status) {
        this.name = name;
        this.projectManager = projectManager;
        this.status = status;
    }
    public Project(String name,  String projectManager, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }


    // ========================================
    // GETTERS / SETTERS
    // ========================================
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getProjectManager() { return projectManager; }
    public void setProjectManager(String projectManager) { this.projectManager = projectManager; }
    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }

    // ==============================
    // Utility Methods
    // ==============================
   /* public void addUser(User user) {
        users.add(user);
        user.getProjects().add(this); // synchronise le côté inverse
    }

    public void removeUser(User user) {
        users.remove(user);
        user.getProjects().remove(this);
    }*/

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project other = (Project) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
