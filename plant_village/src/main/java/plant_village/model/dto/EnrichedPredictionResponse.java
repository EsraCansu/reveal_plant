package plant_village.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

/**
 * Enhanced Prediction Response DTO
 * 
 * Contains complete prediction information including:
 * - %50 Rule validation status
 * - Smart health indicators
 * - Tree structure results (Top 3)
 * - Disease/Plant information with treatment and symptoms
 * - Confidence metrics
 * 
 * This DTO is returned to the Frontend for dynamic UI rendering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedPredictionResponse {
    
    @JsonProperty("prediction_id")
    private Integer predictionId;
    
    @JsonProperty("user_id")
    private Integer userId;
    
    /**
     * %50 Rule Validation Result
     * true: confidence >= 50% (Valid prediction)
     * false: confidence < 50% (Invalid - Not Predictable)
     */
    @JsonProperty("is_valid")
    private Boolean isValid;
    
    /**
     * Smart Health Indicator
     * true: if "healthy" is in the prediction label
     * false: if disease is detected
     */
    @JsonProperty("is_healthy")
    private Boolean isHealthy;
    
    /**
     * Top prediction confidence (0.0 - 1.0)
     */
    @JsonProperty("confidence")
    private Double confidence;
    
    /**
     * Top prediction label from ML model
     * e.g., "Apple___Apple_scab", "Tomato___healthy"
     */
    @JsonProperty("top_prediction")
    private String topPrediction;
    
    /**
     * Type of prediction: "PLANT_PREDICTION" or "DISEASE_PREDICTION"
     */
    @JsonProperty("prediction_type")
    private String predictionType;
    
    /**
     * Timestamp of prediction
     */
    @JsonProperty("created_at")
    private String createdAt;
    
    /**
     * Uploaded image URL
     */
    @JsonProperty("uploaded_image_url")
    private String uploadedImageUrl;
    
    /**
     * Top 3 predictions from Tree structure
     * Each represents a branch in the prediction tree
     */
    @JsonProperty("top_3_results")
    private List<Top3Result> top3Results;
    
    /**
     * Disease information (if isHealthy == false)
     * Contains treatment, symptoms, prevention
     */
    @JsonProperty("disease_info")
    private DiseaseInfo diseaseInfo;
    
    /**
     * Plant information (if isHealthy == true or plant recognition)
     * Contains care instructions, scientific name
     */
    @JsonProperty("plant_info")
    private PlantInfo plantInfo;
    
    /**
     * Nested class representing a single top 3 result
     * (Tree branch)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Top3Result {
        /** Label/Name of the prediction */
        private String label;
        
        /** Confidence score for this prediction */
        private Double confidence;
        
        /** Type: "PLANT" or "DISEASE" */
        private String type;
    }
    
    /**
     * Nested class containing disease details
     * Used when isHealthy == false
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiseaseInfo {
        /** Disease ID from database */
        private Integer diseaseId;
        
        /** Disease name */
        private String name;
        
        /** Scientific name (Latin) */
        private String scientificName;
        
        /** Detailed description of symptoms */
        private String symptomDescription;
        
        /** Treatment methods and recommendations */
        private String treatment;
        
        /** Prevention methods to avoid this disease */
        private String preventionMethods;
        
        /** Which plant parts are affected */
        private String affectedParts;
        
        /** Severity level: "Low", "Medium", "High" */
        private String severityLevel;
    }
    
    /**
     * Nested class containing plant details
     * Used when isHealthy == true or for plant recognition
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlantInfo {
        /** Plant ID from database */
        private Integer plantId;
        
        /** Plant name */
        private String plantName;
        
        /** Scientific name (Latin) */
        private String scientificName;
        
        /** Description of the plant */
        private String description;
        
        /** Care instructions for the plant */
        private String careInstructions;
        
        /** Common diseases that affect this plant */
        private String commonDiseases;
    }
}
