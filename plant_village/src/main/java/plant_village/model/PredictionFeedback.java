package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    
    @Column(name = "user_id", nullable = false)
    private Integer userId = 0;  // 0 = anonim kullanıcı
    
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
    
    @Column(name = "is_approved_from_admin", nullable = false)
    private Boolean isApprovedFromAdmin;
    
    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
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
    }
}
