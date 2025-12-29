package plant_village.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Prediction_Plant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PredictionPlantId.class)
public class PredictionPlant {
    
    @Id
    @Column(name = "prediction_id")
    private Integer predictionId;
    
    @Id
    @Column(name = "plant_id")
    private Integer plantId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plant_id", nullable = false, insertable = false, updatable = false)
    private Plant plant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false, insertable = false, updatable = false)
    private Prediction prediction;
}