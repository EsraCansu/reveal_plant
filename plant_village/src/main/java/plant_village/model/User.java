package plant_village.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "[User]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"predictions", "feedbacks", "logs"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;
    
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "role", length = 10)
    private String role;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "location", length = 100)
    private String location;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    /**
     * Set default values before persisting
     * This ensures all required fields have values even if not sent from frontend
     */
    @PrePersist
    public void prePersist() {
        if (this.lastLogin == null) {
            this.lastLogin = LocalDateTime.now();
        }
        if (this.role == null) {
            this.role = "USER";
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}
