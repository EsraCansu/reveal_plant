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
    
    // Establishing One-to-One relationship with Prediction (onay durumu için)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false, unique = true)
    private Prediction prediction;
    
    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Kritik Sütunlar
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect; // Kullanıcının beyanı: true = doğru, false = yanlış
    
    @Builder.Default
    @Column(name = "is_approved", columnDefinition = "BIT DEFAULT 0")
    private Boolean isApproved = false; // Admin onayı. True ise "doğrulanmış görsel"
    
    @Column(name = "feedback_text", columnDefinition = "NVARCHAR(MAX)")
    private String feedbackText; // Kullanıcı yorumu
    
    @Column(name = "admin_notes", columnDefinition = "NVARCHAR(MAX)")
    private String adminNotes; // Admin notları
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Son güncelleme zamanı
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isApproved == null) {
            isApproved = false;
        }
    }
}
