package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Prediction_Feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PredictionFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false)
    @JsonIgnoreProperties({"plantDetails", "diseaseDetails", "logDetails", "feedbackDetails"})
    private Prediction prediction;
    
    // user_id removed - access via prediction.getUser()
    
    @Column(name = "is_correct")
    private Boolean isCorrect;
    
    @Column(name = "is_approved_from_admin", nullable = false)
    private Boolean isApprovedFromAdmin;

    @Column(name = "comment", columnDefinition = "NVARCHAR(500)")
    private String comment;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonIgnore  // Redundant - use createdAt instead
    private LocalDateTime updatedAt;
    
    @Column(name = "image_added_to_db")
    private Boolean imageAddedToDb;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isCorrect == null) {
            isCorrect = false;
        }
        if (isApprovedFromAdmin == null) {
            isApprovedFromAdmin = false;
        }
        if (imageAddedToDb == null) {
            imageAddedToDb = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get user via prediction (user_id removed from this table)
     * Note: Not serialized to avoid lazy loading issues
     */
    @JsonIgnore
    public User getUser() {
        return prediction != null ? prediction.getUser() : null;
    }
}
