package plant_village.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionDiseaseId implements Serializable {
    private Integer predictionId;
    private Integer diseaseId;
}
