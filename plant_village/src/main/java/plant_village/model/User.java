package plant_village.model;
import plant_village.model.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "User")
public class User {

    // Attributs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private Integer userId;

    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(name = "password_hash", length = 100, nullable = false)
    private String passwordHash;

    @Transient
    private String password; // Geçici olarak şifreyi tutan alan (DB'ye kaydedilmez)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login", nullable = true)
    private LocalDateTime lastLogin;

    @Column(length = 10, nullable = true)
    private String role;
    
    // Relations: User has many Prediction and PredictionLog record (OneToMany)
    
    // User -> Prediction (user_id's foreign key in prediction table)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Prediction> predictions = new HashSet<>();
    
    // User -> PredictionLog (admin_user_id's foreign key in predictionLog table)
    @OneToMany(mappedBy = "adminUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PredictionLog> adminLogs = new HashSet<>();


    // Constructors
    public User() {
    }
    
    public User(String userName, String email, String passwordHash, LocalDateTime createdAt, String role) {
        this.userName = userName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.role = role;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(Set<Prediction> predictions) {
        this.predictions = predictions;
    }

    public Set<PredictionLog> getAdminLogs() {
        return adminLogs;
    }

    public void setAdminLogs(Set<PredictionLog> adminLogs) {
        this.adminLogs = adminLogs;
    }

    // Method toString() (Kısaltıldı)
    @Override
    public String toString() {
        return "User{id=" + userId + ", userName='" + userName + "'}";
    }
}