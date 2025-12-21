package plant_village.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import plant_village.model.Prediction;
import plant_village.model.PredictionFeedback;
import plant_village.model.User;
import plant_village.service.PredictionService;
import plant_village.service.PredictionFeedbackService;
import plant_village.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/predictions")
@Tag(name = "Prediction Management", description = "API for plant disease prediction analysis and management")
public class PredictionController {

    private final PredictionService predictionService;
    private final UserService userService;
    private final PredictionFeedbackService feedbackService;

    @Autowired
    public PredictionController(PredictionService predictionService, UserService userService, PredictionFeedbackService feedbackService) {
        this.predictionService = predictionService;
        this.userService = userService;
        this.feedbackService = feedbackService;
    }

    /**
     * Yeni bir tahmin kaydı oluşturur (FastAPI'dan gelen POST isteği)
     * POST /api/predictions
     * @param prediction Tahmin verisi (ML çıktısı ve kullanıcı ID'si dahil)
     * @return Yeni tahmin nesnesi ve HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<Prediction> createPrediction(@RequestBody Prediction prediction) {
        // Not: Gerçekte, isteğin başlığından (header) gelen JWT ile kullanıcı kimlik doğrulaması yapılır.
        // Şimdilik sadece kullanıcı nesnesinin Prediction içinde geldiğini varsayıyoruz.
        
        Prediction newPrediction = predictionService.createPrediction(prediction);
        return new ResponseEntity<>(newPrediction, HttpStatus.CREATED);
    }

    /**
     * Bir kullanıcıya ait tüm tahmin geçmişini getirir.
     * GET /api/predictions/history/{userId}
     * @param userId Yolda belirtilen kullanıcı ID'si
     * @return Tahmin listesi ve HTTP 200 OK
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Prediction>> getPredictionHistory(@PathVariable Integer userId) {
        List<Prediction> history = predictionService.getPredictionHistory(userId);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    /**
     * Yöneticiler için tahmin düzeltme uç noktası.
     * PUT /api/predictions/{predictionId}
     * @param predictionId Düzeltilecek tahminin ID'si
     * @param updatedPrediction Güncel veri (isValid, confidence vb.)
     * @param adminId Düzeltmeyi yapan yöneticinin ID'si (Normalde JWT'den alınır)
     * @return Güncellenmiş tahmin nesnesi
     */
    @PutMapping("/{predictionId}")
    public ResponseEntity<?> updatePrediction(
            @PathVariable Integer predictionId,
            @RequestBody Prediction updatedPrediction,
            @RequestParam Integer adminId) {
        
        // İş Mantığı: Yönetici var mı?
        User adminUser = userService.findById(adminId)
                                    .orElseThrow(() -> new plant_village.exception.ResourceNotFoundException("Yönetici kullanıcı bulunamadı."));
        
        // İş Mantığı: Yönetici rolüne sahip mi? (Gerçek projede yapılır)
        // if (!adminUser.getRole().equals("ADMIN")) { ... }
        
        try {
            Prediction result = predictionService.updatePrediction(predictionId, updatedPrediction, adminUser);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // ============================================
    // PREDICTION FEEDBACK ENDPOINTS
    // ============================================

    /**
     * Submit user feedback for a prediction (like/dislike)
     * POST /api/predictions/{predictionId}/feedback
     * @param predictionId The prediction being rated
     * @param feedbackRequest Feedback data (userId, isCorrect, comment, etc.)
     * @return Success message
     */
    @PostMapping("/{predictionId}/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @PathVariable Integer predictionId,
            @RequestBody Map<String, Object> feedbackRequest) {
        try {
            // Extract data from request
            Integer userId = (Integer) feedbackRequest.get("userId");
            String predictionType = (String) feedbackRequest.get("predictionType");
            String imageUrl = (String) feedbackRequest.get("imageUrl");
            String predictedName = (String) feedbackRequest.get("predictedName");
            Boolean isCorrect = (Boolean) feedbackRequest.get("isCorrect");
            String comment = (String) feedbackRequest.getOrDefault("comment", "");
            
            // Get user
            User user = userService.findById(userId)
                .orElseThrow(() -> new plant_village.exception.ResourceNotFoundException("User not found"));
            
            // Get prediction
            Prediction prediction = predictionService.findById(predictionId)
                .orElseThrow(() -> new plant_village.exception.ResourceNotFoundException("Prediction not found"));
            
            // Create feedback entity
            PredictionFeedback feedback = PredictionFeedback.builder()
                .prediction(prediction)
                .user(user)
                .predictionType(predictionType)
                .imageUrl(imageUrl)
                .predictedName(predictedName)
                .isCorrect(isCorrect)
                .comment(comment.toString())
                .build();
            
            // Submit feedback (automatically adds image if positive)
            PredictionFeedback savedFeedback = feedbackService.submitFeedback(feedback);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("feedbackId", savedFeedback.getFeedbackId());
            response.put("message", savedFeedback.getIsCorrect() 
                ? "Thank you! Your feedback helps improve our model. Image added to our database." 
                : "Thank you for your feedback!");
            response.put("imageAdded", savedFeedback.getImageAddedToDb());
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all feedback (admin only)
     * GET /api/predictions/feedback/all
     * @return List of all feedback
     */
    @GetMapping("/feedback/all")
    public ResponseEntity<List<PredictionFeedback>> getAllFeedback() {
        List<PredictionFeedback> feedback = feedbackService.getAllFeedback();
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    /**
     * Get feedback statistics (admin only)
     * GET /api/predictions/feedback/stats
     * @return Feedback statistics (total, correct, incorrect, accuracy)
     */
    @GetMapping("/feedback/stats")
    public ResponseEntity<Map<String, Object>> getFeedbackStats() {
        Map<String, Object> stats = feedbackService.getFeedbackStatistics();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    /**
     * Get feedback for a specific prediction
     * GET /api/predictions/{predictionId}/feedback
     * @param predictionId The prediction ID
     * @return List of feedback for this prediction
     */
    @GetMapping("/{predictionId}/feedback")
    public ResponseEntity<List<PredictionFeedback>> getFeedbackByPrediction(@PathVariable Integer predictionId) {
        List<PredictionFeedback> feedback = feedbackService.getFeedbackByPredictionId(predictionId);
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    /**
     * Get feedback by user
     * GET /api/predictions/feedback/user/{userId}
     * @param userId The user ID
     * @return List of feedback submitted by this user
     */
    @GetMapping("/feedback/user/{userId}")
    public ResponseEntity<List<PredictionFeedback>> getFeedbackByUser(@PathVariable Integer userId) {
        List<PredictionFeedback> feedback = feedbackService.getFeedbackByUserId(userId);
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    /**
     * Analyze plant image using ML model (Frontend endpoint)
     * POST /api/predictions/analyze
     * Workflow: Image → FastAPI → %50 Rule → Tree + Stack + Hash Map → Response
     * 
     * @param request Map containing imageBase64, predictionType, userId, description
     * @return Frontend-compatible response with predicted_class, confidence, top_predictions
     */
    @Operation(
        summary = "Analyze plant disease from image",
        description = "Main endpoint for analyzing plant images using ResNet-101 model. " +
                      "Returns disease prediction with confidence score and top 3 possibilities. " +
                      "Implements 7-step workflow: FastAPI ML analysis → %50 confidence threshold → " +
                      "Tree structure (hierarchical storage) → Stack (LIFO processing) → Hash Map (O(1) lookups)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Analysis successful - Returns predicted disease and confidence score",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - Missing required fields (imageBase64)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error - ML model or database failure",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzePlantImage(
            @Parameter(
                description = "Request payload containing base64 encoded image and metadata",
                required = true,
                schema = @Schema(
                    example = "{\"imageBase64\": \"data:image/jpeg;base64,/9j/4AAQSkZJRg...\", " +
                              "\"userId\": 1, \"predictionType\": \"detect-disease\", " +
                              "\"description\": \"Uploaded plant image\"}"
                )
            )
            @RequestBody Map<String, Object> request) {
        
        try {
            String imageBase64 = (String) request.get("imageBase64");
            String predictionType = (String) request.getOrDefault("predictionType", "detect-disease");
            Integer userId = (Integer) request.getOrDefault("userId", 1);
            String description = (String) request.getOrDefault("description", "Uploaded plant image");
            
            log.info("📸 Analyzing plant image for user: {}, type: {}", userId, predictionType);
            
            // Call the main prediction workflow (Tree + Stack + Hash Map logic inside)
            Prediction prediction = predictionService.predictPlantDisease(
                userId,
                null,
                imageBase64,
                description
            );
            
            // Build response matching frontend expectations
            Map<String, Object> response = new HashMap<>();
            
            // Use topPrediction field which contains the actual ML result (e.g., "Tomato___Leaf_Mold")
            String predictedClass = prediction.getTopPrediction() != null ? prediction.getTopPrediction() : "Unknown";
            response.put("predicted_class", predictedClass);
            response.put("confidence", prediction.getConfidence() != null ? prediction.getConfidence() : 0.0);
            response.put("is_valid", prediction.getIsValid());
            
            // Determine isHealthy from predicted class name
            boolean isHealthy = predictedClass.toLowerCase().contains("healthy");
            response.put("is_healthy", isHealthy);
            response.put("description", "Analysis completed");
            response.put("prediction_id", prediction.getId());
            
            // Top 3 predictions from description field (stored as JSON)
            List<Map<String, Object>> topPredictions = new java.util.ArrayList<>();
            String top3Json = prediction.getDescription();
            if (top3Json != null && top3Json.startsWith("[")) {
                try {
                    // Parse simple JSON manually
                    String cleaned = top3Json.replace("[", "").replace("]", "");
                    String[] preds = cleaned.split("\\},\\{");
                    for (String pred : preds) {
                        pred = pred.replace("{", "").replace("}", "");
                        String[] parts = pred.split(",");
                        if (parts.length >= 2) {
                            String disease = parts[0].split(":")[1].replace("\"", "").trim();
                            String conf = parts[1].split(":")[1].trim();
                            Map<String, Object> p = new HashMap<>();
                            p.put("class_name", disease);
                            p.put("probability", Double.parseDouble(conf));
                            topPredictions.add(p);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse top predictions: {}", e.getMessage());
                }
            }
            response.put("top_predictions", topPredictions);
            
            log.info("✅ Prediction successful: ID={}, isValid={}, isHealthy={}, confidence={}", 
                prediction.getId(), prediction.getIsValid(), isHealthy, prediction.getConfidence());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("❌ Prediction error: {}", e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("message", "Failed to analyze plant image");
            
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
