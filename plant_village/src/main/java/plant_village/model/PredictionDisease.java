package plant_village.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Prediction_Disease")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PredictionDiseaseId.class)
public class PredictionDisease {
    
    @Id
    @Column(name = "prediction_id")
    private Integer predictionId;
    
    @Id
    @Column(name = "disease_id")
    private Integer diseaseId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disease_id", nullable = false, insertable = false, updatable = false)
    private Disease disease;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false, insertable = false, updatable = false)
    private Prediction prediction;
    
    @Column(name = "is_healthy")
    private Boolean isHealthy;
    
    @Column(name = "confidence")
    private Double confidence;
}
