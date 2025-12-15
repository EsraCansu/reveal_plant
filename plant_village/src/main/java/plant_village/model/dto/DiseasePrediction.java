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
}
