package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * STEP 4A: Link Plant to Prediction
 * Plant Linking Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionPlantLinkRequest {

    @NotNull(message = "Plant ID boş olamaz")
    private Integer plantId;
}
