package plant_village.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Prediction Entity representing the 'Prediction' table in MSSQL.
 * This class acts as a Data Transfer Object and a Database Entity.
 */
@Entity
@Table(name = "Prediction")
@Data // Generates Getters, Setters, toString, etc. via Lombok
@NoArgsConstructor // Default constructor for JPA
@AllArgsConstructor // Parameterized constructor for Builder pattern
@Builder // Enables fluent API for object creation
public class Prediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Integer id; // Primary Key: Unique identifier for each prediction
    
    // Establishing a Many-to-One relationship with the User entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Reference to the user who performed the prediction

    @Column(name = "prediction_type", length = 50)
    private String predictionType; // Type of analysis (e.g., Plant or Disease)
    
    @Column(name = "confidence")
    private Double confidence; // The accuracy score provided by ResNet-101
    
    @Column(name = "uploaded_image_url")
    private String uploadedImageUrl; // Storage path of the analyzed image
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // Timestamp of the transaction
    
    @Column(name = "is_valid")
    private Boolean isValid; // Flag to verify the success of the AI analysis

    @Column(name = "top_prediction", length = 200)
    private String topPrediction; // Top prediction class name from ML model
    
    @Column(name = "is_healthy")
    private Boolean isHealthy; // Smart indicator: contains "healthy"?
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Optional description or analysis notes

    // --- Relationship Mapping ---
    // One-to-Many connection: One prediction can yield multiple plant details
    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    private List<PredictionPlant> plantDetails;

    // One-to-Many connection: Linking analysis results to specific diseases
    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    private List<PredictionDisease> diseaseDetails;

    // One-to-Many connection: Keeping track of system logs for this prediction
    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    private List<PredictionLog> logDetails;
}

