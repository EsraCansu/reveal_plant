package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "Prediction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "prediction_type", length = 50)
    private String predictionType;
    
    @Column(name = "confidence")
    private Double confidence;
    
    @Column(name = "uploaded_image_url")
    private String uploadedImageUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "is_valid")
    private Boolean isValid;

    // --- One-to-One connections ---
    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    private List<PredictionPlant> plantDetails;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    private List<PredictionDisease> diseaseDetails;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    private List<PredictionLog> logDetails;

    // we connect the datas where from FASTAPI
}
