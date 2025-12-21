package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * PredictionFeedback Entity representing the 'Prediction_Feedback' table in MSSQL.
 * Stores user ratings (like/dislike) on predictions to improve model accuracy.
 */
@Entity
@Table(name = "Prediction_Feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "prediction_type", length = 50, nullable = false)
    private String predictionType; // 'PLANT' or 'DISEASE'
    
    @Column(name = "image_url", columnDefinition = "VARCHAR(MAX)", nullable = false)
    private String imageUrl;
    
    @Column(name = "predicted_name", length = 200)
    private String predictedName;
    
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect; // true = like/correct, false = dislike/incorrect
    
    @Column(name = "comment", length = 500)
    private String comment;
    
    @Column(name = "image_added_to_db", nullable = false)
    private Boolean imageAddedToDb = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<PlantUserImages> plantUserImages;
    
    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<DiseaseUserImages> diseaseUserImages;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (imageAddedToDb == null) {
            imageAddedToDb = false;
        }
    }
}
