package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STEP 4A: Link Plant to Prediction
 * Plant Linking Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionPlantLinkResponse {

    private Integer predictionPlantId;
    private Integer predictionId;
    private Integer plantId;
    private Float confidence;
    
    // Plant details
    private String plantName;
    private String scientificName;
    private String description;
    private String careTips;
    
    private String message;
}
