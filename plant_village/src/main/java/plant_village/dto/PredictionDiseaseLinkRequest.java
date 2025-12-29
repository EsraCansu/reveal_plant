package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * STEP 4B: Link Disease to Prediction
 * Disease Linking Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDiseaseLinkRequest {

    @NotNull(message = "Disease ID boş olamaz")
    private Integer diseaseId;

    @NotNull(message = "Is Healthy boş olamaz")
    private Boolean isHealthy;
}
