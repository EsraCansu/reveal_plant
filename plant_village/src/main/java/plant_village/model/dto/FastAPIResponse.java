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
public class FastAPIResponse {
    private String status;
    private String message;
    
    @JsonProperty("top_prediction")
    private String topPrediction;
    
    @JsonProperty("top_confidence")
    private Double topConfidence;
    
    @JsonProperty("recommended_action")
    private String recommendedAction;
    
    private java.util.List<DiseasePrediction> predictions;
}
