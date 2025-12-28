package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STEP 2: Dictionary
 * Plant Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantResponse {

    private Integer plantId;
    private String plantName;
    private String scientificName;
    private String imageUrl;
    private String description;
    private String careTips;
    private String wateringFrequency;
    private String sunlightRequirement;
    private String soilType;
    private String hardinessZone;
    private Boolean validClassification;
}
