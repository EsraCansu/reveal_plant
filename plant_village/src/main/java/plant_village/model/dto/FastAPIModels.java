package plant_village.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * DTO for FastAPI ML Model prediction response
 * Expected format from FastAPI /predict endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FastAPIResponse {
    private String status;                          // "success" or "error"
    private String message;                         // Description
    private Map<String, Double> predictions;        // Disease name -> confidence (0-1)
    private String topPrediction;                   // Most likely disease name
    private Double topConfidence;                   // Highest confidence score
    private String recommendedAction;               // Treatment recommendation
    private List<String> symptoms;                  // Detected symptoms
    private Map<String, Object> metadata;           // Additional metadata
    private Long processingTimeMs;                  // How long prediction took
}

/**
 * DTO for FastAPI prediction request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FastAPIPredictionRequest {
    private String imageBase64;                     // Base64 encoded image
    private String imageType;                       // jpg, png, etc.
    private Long plantId;                           // Plant ID for context
    private String description;                     // User description
}

/**
 * Simplified disease prediction result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseasePrediction {
    private String diseaseName;
    private Double confidence;
    private String recommendation;
}
