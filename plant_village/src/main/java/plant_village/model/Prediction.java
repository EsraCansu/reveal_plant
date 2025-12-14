package plant_village.model;
import plant_village.model.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Prediction")
public class Prediction {

    // Attributs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id", unique = true, nullable = false)
    private Integer predictionId;

    // one to one relation
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "prediction_type", length = 10, nullable = true)
    private String predictionType;

    @Column(nullable = true)
    private Float confidence; 

    @Column(name = "uploaded_image_url", length = 255, nullable = true)
    private String uploadedImageUrl; 

    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt; 

    @Column(name = "is_valid", nullable = true)
    private Boolean isValid; 

    // (@ManyToMany)
    // MappedBy: plant class manage predictions
    @ManyToMany(mappedBy = "predictions")
    private Set<Plant> plants = new HashSet<>();
    
    // (OneToMany)
    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PredictionLog> logs = new HashSet<>();
    
    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PredictionDisease> diseases = new HashSet<>();


    // Constructors
    public Prediction() {
    }

    public Prediction(Integer predictionId, String predictionType, Float confidence, String uploadedImageUrl, LocalDateTime createdAt, Boolean isValid) {
        this.predictionId = predictionId;
        this.predictionType = predictionType;
        this.confidence = confidence;
        this.uploadedImageUrl = uploadedImageUrl;
        this.createdAt = createdAt;
        this.isValid = isValid; 
    }


    // Getters and Setters
    public Integer getPredictionId() {
        return predictionId;
    }
    
    public void setPredictionId(Integer predictionId) {
        this.predictionId = predictionId;
    }
    
    public String getPredictionType() {
        return predictionType;
    }
    
    public void setPredictionType(String predictionType) {
        this.predictionType = predictionType;
    }
    
    public Float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }
    
    public String getUploadedImageUrl() {
        return uploadedImageUrl;
    }
    
    public void setUploadedImageUrl(String uploadedImageUrl) {
        this.uploadedImageUrl = uploadedImageUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getIsValid() {
        return isValid;
    }
    
    public void setIsValid(Boolean isValid) {
        this.isValid = isValid ;
    }
    
    public Set<Plant> getPlants() {
        return plants;
    }

    public void setPlants(Set<Plant> plants) {
        this.plants = plants;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public Set<PredictionLog> getLogs() {
        return logs;
    }

    public void setLogs(Set<PredictionLog> logs) {
        this.logs = logs;
    }
    
    public Set<PredictionDisease> getDiseases() {
        return diseases;
    }

    public void setDiseases(Set<PredictionDisease> diseases) {
        this.diseases = diseases;
    }

    // Method toString()

    @Override
    public String toString() {
        // ... toString içeriği
        return "Prediction{" +
                "predictionId=" + predictionId +
                ", predictionType=" + predictionType +
                ", confidence=" + confidence +
                ", uploadedImageUrl=" + uploadedImageUrl +
                ", createdAt=" + createdAt +
                ", isValid=" + isValid +
                '}';
    }
}