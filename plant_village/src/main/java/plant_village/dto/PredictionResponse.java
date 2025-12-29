package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * STEP 3: Main Prediction
 * Prediction Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {

    private Integer predictionId;
    private Integer userId;
    private String predictionType;
    private Float confidence;
    private String uploadedImageUrl;
    private LocalDateTime createAt;
    private Boolean isValid;
    private String wateringFrequency;
    private String soilType;
    private String hardinessZone;
    private String message;
}
