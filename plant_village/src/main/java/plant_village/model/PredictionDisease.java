package plant_village.model;
import plant_village.model.*;

import jakarta.persistence.*;

@Entity
@Table(name = "Prediction_Disease")
public class PredictionDisease {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Bu ara tablo için birincil anahtar

    // Bire-Çok İlişkisi: Ait olduğu Prediction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;

    // Bire-Çok İlişkisi: İlişkilendirildiği Disease
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    @Column(name = "is_healthy", nullable = true)
    private Boolean isHealthy;


    // Constructors
    public PredictionDisease() {
    }
    
    public PredictionDisease(Prediction prediction, Disease disease, Boolean isHealthy) {
        this.prediction = prediction;
        this.disease = disease;
        this.isHealthy = isHealthy;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }
    
    public Disease getDisease() {
        return disease;
    }

    public void setDisease(Disease disease) {
        this.disease = disease;
    }

    public Boolean getIsHealthy() {
        return isHealthy;
    }
    
    public void setIsHealthy(Boolean isHealthy) {
        this.isHealthy = isHealthy;
    }
    
    // Method toString()
    @Override
    public String toString() {
        return "PredictionDisease{id=" + id + ", isHealthy=" + isHealthy + "}";
    }
}
