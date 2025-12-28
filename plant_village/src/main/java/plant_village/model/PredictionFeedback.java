package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "predictions"})
    private User user;
    
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
    
    @Column(name = "is_approved_from_admin", nullable = false)
    private Boolean isApprovedFromAdmin;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @Column(name = "comment", columnDefinition = "NVARCHAR(500)")
    private String comment;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "image_added_to_db")
    private Boolean imageAddedToDb;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isApprovedFromAdmin == null) {
            isApprovedFromAdmin = false;
        }
        if (isCorrect == null) {
            isCorrect = false;
        }
        if (imageAddedToDb == null) {
            imageAddedToDb = false;
        }
        // Set isApproved based on isApprovedFromAdmin
        if (isApproved == null) {
            isApproved = isApprovedFromAdmin != null && isApprovedFromAdmin ? true : false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Always sync isApproved with isApprovedFromAdmin on update
        if (isApprovedFromAdmin != null) {
            isApproved = isApprovedFromAdmin;
        }
    }
}
