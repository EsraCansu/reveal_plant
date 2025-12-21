package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DiseaseUserImages Entity representing the 'Disease_User_Images' table in MSSQL.
 * Stores validated disease images submitted by users through positive feedback.
 */
@Entity
@Table(name = "Disease_User_Images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseUserImages {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;
    
    @Column(name = "disease_name", length = 50, nullable = false)
    private String diseaseName;
    
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
