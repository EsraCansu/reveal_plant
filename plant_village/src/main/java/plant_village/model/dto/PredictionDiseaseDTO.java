package plant_village.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionDiseaseDTO {
    private Integer predictionId;
    private Integer diseaseId;
    private String diseaseName;
    private Boolean isHealthy;
    private Double confidence;
}
