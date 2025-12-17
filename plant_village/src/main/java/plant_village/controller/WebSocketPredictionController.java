package plant_village.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import plant_village.model.*;
import plant_village.service.PredictionService;

import java.time.LocalDateTime;

/**
 * WebSocket Message Controller
 * 
 * Handles real-time prediction requests/responses via WebSocket (STOMP protocol).
 * 
 * Message Flow:
 * 1. Client sends to: /app/predict/{userId}
 * 2. Handler processes and broadcasts to: /topic/predictions
 * 3. Pushes individual results to: /user/queue/predictions/{userId}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketPredictionController {

    private final PredictionService predictionService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Endpoint: /app/predict/{userId}
     * 
     * Receives prediction requests from clients and processes them in real-time.
     * Sends updates back to the requesting user via /user/queue/predictions/{userId}
     * and broadcasts to all subscribers of /topic/predictions
     */
    @MessageMapping("/predict/{userId}")
    public void processPrediction(
            @Payload PredictionRequest request,
            @DestinationVariable Integer userId) {
        
        try {
            log.info("Received prediction request from user: {}, plant: {}", userId, request.getPlantId());

            // Send status update - PROCESSING
            sendStatusUpdate(userId, null, "PROCESSING", 10, "Processing prediction request...");

            // Validate request
            if (request.getImageBase64() == null || request.getImageBase64().isEmpty()) {
                sendError(userId, "INVALID_INPUT", "Image data is required");
                return;
            }

            // Send status update - ANALYZING
            sendStatusUpdate(userId, null, "ANALYZING", 30, "Analyzing plant image...");

            // Call prediction service
            Prediction prediction = predictionService.predictPlantDisease(
                    userId,
                    request.getPlantId(),
                    request.getImageBase64(),
                    request.getDescription()
            );

            // Send status update - COMPLETE
            sendStatusUpdate(userId, prediction.getId(), "COMPLETE", 100, "Prediction completed successfully");

            // Prepare response - TODO: Fix null pointer exceptions when relations are null
            PredictionResponse response = PredictionResponse.builder()
                    .predictionId(prediction.getId())
                    .userId(userId)
                    .plantId(request.getPlantId())
                    .plantName("Unknown")
                    .diseaseName("Unknown")
                    .confidence(prediction.getConfidence().doubleValue())
                    .recommendedAction("Pending")
                    .predictedAt(prediction.getCreatedAt())
                    .status("SUCCESS")
                    .message("Prediction completed successfully")
                    .build();

            // Send to individual user
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/predictions",
                    response
            );

            // Broadcast to all subscribers of /topic/predictions
            messagingTemplate.convertAndSend("/topic/predictions", response);

            log.info("Prediction sent to user {} with ID {}", userId, prediction.getId());

        } catch (Exception e) {
            log.error("Error processing prediction for user {}: {}", userId, e.getMessage(), e);
            sendError(userId, "PREDICTION_ERROR", e.getMessage());
        }
    }

    /**
     * Send status update to specific user
     */
    private void sendStatusUpdate(Integer userId, Integer predictionId, String status, 
                                   Integer progress, String message) {
        PredictionStatusUpdate update = PredictionStatusUpdate.builder()
                .predictionId(predictionId)
                .status(status)
                .progressPercentage(progress)
                .message(message)
                .updatedAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/status",
                update
        );
    }

    /**
     * Send error message to specific user
     */
    private void sendError(Integer userId, String errorCode, String errorMessage) {
        WebSocketError error = WebSocketError.builder()
                .errorCode(errorCode)
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/errors",
                error
        );
    }

    /**
     * Endpoint: /app/heartbeat
     * 
     * Optional health check for WebSocket connection.
     * Clients can send periodic heartbeats to keep connection alive.
     */
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload String clientId) {
        log.debug("Heartbeat received from client: {}", clientId);
    }
}
