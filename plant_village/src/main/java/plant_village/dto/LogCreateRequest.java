package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * STEP 6: Create Audit Log
 * Log Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogCreateRequest {

    @NotNull(message = "Prediction ID boş olamaz")
    private Integer predictionId;

    @NotBlank(message = "Action Type boş olamaz")
    private String actionType; // PLANT_PREDICTION_CREATED, DISEASE_PREDICTION_CREATED, etc.
}
