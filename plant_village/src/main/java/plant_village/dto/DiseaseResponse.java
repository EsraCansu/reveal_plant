package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STEP 2: Dictionary
 * Disease Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseResponse {

    private Integer diseaseId;
    private String diseaseName;
    private String symptomDescription;
    private String cause;
    private String exampleImageUrl;
    private String treatment;
}
