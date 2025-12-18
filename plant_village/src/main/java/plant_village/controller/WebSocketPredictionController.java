package plant_village.controller; // Sadece bu kalmalı, ikinci package satırını sildik.

import plant_village.model.dto.*;
import plant_village.model.Prediction;
import plant_village.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;

/**
 * WebSocket Message Controller
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketPredictionController {

    private final PredictionService predictionService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/predict/{userId}")
    public void processPrediction(
            @Payload PredictionRequest request,
            @DestinationVariable Integer userId) {
        
        try {
            log.info("Received prediction request from user: {}, plant: {}", userId, request.getPlantId());

            sendStatusUpdate(userId, null, "PROCESSING", 10, "Processing prediction request...");

            if (request.getImageBase64() == null || request.getImageBase64().isEmpty()) {
                sendError(userId, "INVALID_INPUT", "Image data is required");
                return;
            }

            sendStatusUpdate(userId, null, "ANALYZING", 30, "Analyzing plant image...");

            // Service metodunu çağırıyoruz
            Prediction prediction = predictionService.predictPlantDisease(
                    userId.intValue(), // Integer'a çevirdik (Modelimizle uyum için)
                    request.getPlantId(),
                    request.getImageBase64(),
                    request.getDescription()
            );

            sendStatusUpdate(userId, prediction.getId(), "COMPLETE", 100, "Prediction completed successfully");

            PredictionResponse response = PredictionResponse.builder()
                    .predictionId(prediction.getId())
                    .userId(userId)
                    .plantId(request.getPlantId())
                    .plantName("Unknown")
                    .diseaseName("Unknown")
                    .confidence(prediction.getConfidence().doubleValue())
                    .predictedAt(prediction.getCreatedAt())
                    .status("SUCCESS")
                    .message("Prediction completed successfully")
                    .build();

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/predictions",
                    response
            );

            messagingTemplate.convertAndSend("/topic/predictions", response);

        } catch (Exception e) {
            log.error("Error processing prediction for user {}: {}", userId, e.getMessage(), e);
            sendError(userId, "PREDICTION_ERROR", e.getMessage());
        }
    }

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

    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload String clientId) {
        log.debug("Heartbeat received from client: {}", clientId);
    }
}