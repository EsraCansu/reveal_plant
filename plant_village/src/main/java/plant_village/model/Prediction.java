package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Prediction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Prediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore  // Prevent circular reference with User
    private User user;
    
    // Transient field for accepting userId from JSON
    @Transient
    private Integer userId;

    @Column(name = "prediction_type", length = 50)
    private String predictionType;
    
    @Column(name = "confidence")
    private Double confidence;
    
    @Column(name = "uploaded_image_url")
    private String uploadedImageUrl;
    
    @Column(name = "create_at")
    private LocalDateTime createAt;
    
    @Column(name = "is_valid")
    private Boolean isValid;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    @JsonIgnore  // Prevent circular reference
    private List<PredictionPlant> plantDetails;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    @JsonIgnore  // Prevent circular reference
    private List<PredictionDisease> diseaseDetails;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    @JsonIgnore  // Prevent circular reference
    private List<PredictionLog> logDetails;
    
    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    @JsonIgnore  // Prevent circular reference
    private List<PredictionFeedback> feedbackDetails;
}
