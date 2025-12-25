package plant_village.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseasePrediction {
    private String disease;
    
    @JsonProperty("confidence_score")
    private Double confidenceScore;

        @JsonProperty("symptom_description")
        private String symptomDescription;

        @JsonProperty("treatment")
        private String treatment;

        @JsonProperty("recommended_medicines")
        private String recommendedMedicines;
    
}
