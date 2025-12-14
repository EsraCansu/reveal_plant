package plant_village.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Real-time WebSocket message for prediction requests from client to server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionRequest {
    private Long userId;
    private Long plantId;
    private String imageBase64;
    private String description;
    private LocalDateTime requestedAt;
}

/**
 * Real-time WebSocket message for prediction responses from server to client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponse {
    private Long predictionId;
    private Long userId;
    private Long plantId;
    private String plantName;
    private String diseaseName;
    private Double confidence;
    private String recommendedAction;
    private LocalDateTime predictedAt;
    private String status;  // SUCCESS, ERROR, PROCESSING
    private String message;
}

/**
 * Real-time status update for long-running predictions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionStatusUpdate {
    private Long predictionId;
    private String status;  // UPLOADED, PROCESSING, ANALYZING, COMPLETE, ERROR
    private Integer progressPercentage;
    private String message;
    private LocalDateTime updatedAt;
}

/**
 * Error message for WebSocket communication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketError {
    private String errorCode;
    private String errorMessage;
    private LocalDateTime timestamp;
}
