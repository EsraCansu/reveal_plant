package plant_village.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import plant_village.model.Prediction;
import java.util.List;

/**
 * Wrapper class for prediction result containing both 
 * the saved Prediction entity and all ML predictions for "Other Possibilities"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResult {
    
    /** The saved prediction entity (TOP 1 only saved to DB) */
    private Prediction prediction;
    
    /** All ML predictions for displaying "Other Possibilities" in frontend */
    private List<DiseasePrediction> allPredictions;
}
