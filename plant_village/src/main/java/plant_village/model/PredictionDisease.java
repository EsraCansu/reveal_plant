package plant_village.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Prediction_disease")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionDisease {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction; // connection to Prediction Table 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;  // for Disease information in detail
    
    @Column(name = "is_healthy")
    private Boolean isHealthy; // to indicate if the plant is healthy or not
}
