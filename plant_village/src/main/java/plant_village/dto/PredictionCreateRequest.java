package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * STEP 3: Main Prediction
 * Create Prediction Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionCreateRequest {

    @NotNull(message = "User ID boş olamaz")
    private Integer userId;

    @NotBlank(message = "Prediction Type boş olamaz")
    private String predictionType; // "PLANT" or "DISEASE"

    @NotBlank(message = "Image URL boş olamaz")
    private String uploadedImageUrl;

    @NotNull(message = "Confidence boş olamaz")
    @Min(value = 0, message = "Confidence 0 ile 1 arasında olmalıdır")
    @Max(value = 1, message = "Confidence 0 ile 1 arasında olmalıdır")
    private Float confidence;

    @Builder.Default
    private Boolean isValid = true;

    private String wateringFrequency;
    private String careTips;
    private String soilType;
    private String hardinessZone;
}
