package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * PlantUserImages Entity representing the 'Plant_User_Images' table in MSSQL.
 * Stores validated plant images submitted by users through positive feedback.
 */
@Entity
@Table(name = "Plant_User_Images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantUserImages {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "plant_id", nullable = false)
    private Integer plantId;
    
    @Column(name = "plant_name", length = 50, nullable = false)
    private String plantName;
    
    @Column(name = "image_url", columnDefinition = "VARCHAR(MAX)", nullable = false)
    private String imageUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedByUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private PredictionFeedback feedback;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (verified == null) {
            verified = true;
        }
    }
}
