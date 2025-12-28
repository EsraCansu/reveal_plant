package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STEP 4B: Link Disease to Prediction
 * Disease Linking Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDiseaseLinkResponse {

    private Integer predictionDiseaseId;
    private Integer predictionId;
    private Integer diseaseId;
    private Boolean isHealthy;
    private Float confidence;
    
    // Disease details
    private String diseaseName;
    private String symptomDescription;
    private String treatment;
    private String cause;
    
    private String message;
}
