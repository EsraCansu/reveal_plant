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
import plant_village.model.PredictionLog;
import plant_village.model.User;
import plant_village.repository.PredictionLogRepository;
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
    private final PredictionLogRepository logRepository;

    @Autowired
    public PredictionController(PredictionService predictionService, UserService userService, 
                                PredictionFeedbackService feedbackService, PredictionLogRepository logRepository) {
        this.predictionService = predictionService;
        this.userService = userService;
        this.feedbackService = feedbackService;
        this.logRepository = logRepository;
    }

    /**
     * Yeni bir tahmin kaydı oluşturur (FastAPI'dan gelen POST isteği)
     * POST /api/predictions
     * @param prediction Tahmin verisi (ML çıktısı ve kullanıcı ID'si dahil)
     * @return Yeni tahmin nesnesi ve HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<Prediction> createPrediction(@RequestBody Prediction prediction) {
        // userId varsa, User nesnesini veritabanından al
        if (prediction.getUser() == null) {
            Integer userId = prediction.getUserId();
            if (userId != null && userId > 0) {
                User user = userService.findById(userId)
                    .orElse(null);
                if (user != null) {
                    prediction.setUser(user);
                }
            }
        }
        
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
            String feedbackText = (String) feedbackRequest.getOrDefault("feedbackText", "");
            Boolean isCorrect = (Boolean) feedbackRequest.get("isCorrect");
            
            // Get prediction
            Prediction prediction = predictionService.findById(predictionId)
                .orElseThrow(() -> new plant_village.exception.ResourceNotFoundException("Prediction not found"));
            
            // Create feedback entity
            PredictionFeedback feedback = PredictionFeedback.builder()
                .prediction(prediction)
                .isCorrect(isCorrect)
                .comment(feedbackText)
                .isApprovedFromAdmin(false)
                .createdAt(java.time.LocalDateTime.now())
                .build();
            
            // Submit feedback
            PredictionFeedback savedFeedback = feedbackService.submitFeedback(feedback);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("feedbackId", savedFeedback.getFeedbackId());
            response.put("message", "Feedback submitted successfully");
            
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
    public ResponseEntity<?> getAllFeedback() {
        try {
            List<PredictionFeedback> feedbacks = feedbackService.getAllFeedback();
            
            // Convert to simple DTOs to avoid JSON serialization issues
            List<Map<String, Object>> feedbackDTOs = feedbacks.stream().map(fb -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("feedbackId", fb.getFeedbackId());
                dto.put("predictionId", fb.getPrediction() != null ? fb.getPrediction().getId() : null);
                dto.put("isCorrect", fb.getIsCorrect());
                dto.put("isApprovedFromAdmin", fb.getIsApprovedFromAdmin());
                dto.put("feedbackText", fb.getComment());
                dto.put("adminNotes", fb.getComment());
                dto.put("createdAt", fb.getCreatedAt() != null ? fb.getCreatedAt().toString() : null);
                dto.put("updatedAt", fb.getCreatedAt() != null ? fb.getCreatedAt().toString() : null);
                return dto;
            }).collect(java.util.stream.Collectors.toList());
            
            log.info("📋 Retrieved {} feedbacks", feedbackDTOs.size());
            return new ResponseEntity<>(feedbackDTOs, HttpStatus.OK);
        } catch (Exception e) {
            log.error("❌ Error getting feedbacks: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                description,
                predictionType
            );
            

            // Build response matching frontend expectations
            Map<String, Object> response = new HashMap<>();

            // Use predictionType field which contains the actual ML result (e.g., "Tomato___Leaf_Mold")
            String predictedClass = prediction.getPredictionType() != null ? prediction.getPredictionType() : "Unknown";
            response.put("predicted_class", predictedClass);
            response.put("confidence", prediction.getConfidence() != null ? prediction.getConfidence() : 0.0);
            response.put("is_valid", prediction.getIsValid());

            // Determine isHealthy from predicted class name
            boolean isHealthy = predictedClass.toLowerCase().contains("healthy");
            response.put("is_healthy", isHealthy);
            response.put("description", "Analysis completed");
            response.put("prediction_id", prediction.getId());

            // Get cache manager for both plant and disease lookups
            plant_village.util.PlantDiseaseCacheManager cacheManager = predictionService instanceof plant_village.service.impl.PredictionServiceImpl ? 
                ((plant_village.service.impl.PredictionServiceImpl)predictionService).getCacheManager() : null;

            // Plant identification mode - add plant information
            if ("identify-plant".equals(predictionType)) {
                String plantName = parsePlantNameFromMLFormat(predictedClass);
                String plantDescription = "";
                String scientificName = "";
                
                if (cacheManager != null) {
                    java.util.Optional<plant_village.model.Plant> plantOpt = cacheManager.getPlantByName(plantName);
                    if (plantOpt.isPresent()) {
                        plant_village.model.Plant plant = plantOpt.get();
                        plantDescription = plant.getDescription() != null ? plant.getDescription() : "";
                        scientificName = plant.getScientificName() != null ? plant.getScientificName() : "";
                    }
                }
                
                response.put("plant_description", plantDescription);
                response.put("scientific_name", scientificName);
            }

            // Disease detection mode - add disease info for main result (top1)
            String symptomDescription = "";
            String treatment = "";
            if (!isHealthy && "detect-disease".equals(predictionType)) {
                if (cacheManager != null) {
                    java.util.Optional<plant_village.model.Disease> diseaseOpt = cacheManager.getDiseaseByName(predictedClass);
                    if (diseaseOpt.isPresent()) {
                        plant_village.model.Disease disease = diseaseOpt.get();
                        symptomDescription = disease.getSymptomDescription();
                        treatment = disease.getTreatment();
                    }
                }
            }
            response.put("symptom_description", symptomDescription);
            response.put("treatment", treatment);
            response.put("recommended_medicines", "");

            // Top 3 predictions from PredictionDisease relationships
            List<Map<String, Object>> topPredictions = new java.util.ArrayList<>();
            
            // Get disease predictions from relationship table
            if (prediction.getDiseaseDetails() != null && !prediction.getDiseaseDetails().isEmpty()) {
                for (plant_village.model.PredictionDisease pd : prediction.getDiseaseDetails()) {
                    Map<String, Object> p = new HashMap<>();
                    plant_village.model.Disease disease = pd.getDisease();
                    if (disease != null) {
                        p.put("class_name", disease.getDiseaseName());
                        p.put("probability", pd.getMatchConfidence() != null ? pd.getMatchConfidence() : 0.0);
                        p.put("symptom_description", disease.getSymptomDescription() != null ? disease.getSymptomDescription() : "");
                        p.put("treatment", disease.getTreatment() != null ? disease.getTreatment() : "");
                        p.put("recommended_medicines", "");
                        topPredictions.add(p);
                    }
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

    /**
     * Parse plant name from ML model format
     * Converts: "Tomato___Leaf_Mold" → "Tomato"
     * Converts: "Strawberry___healthy" → "Strawberry"
     * 
     * @param mlFormat Plant name in ML format
     * @return Cleaned plant name for database lookup
     */
    private String parsePlantNameFromMLFormat(String mlFormat) {
        if (mlFormat == null || !mlFormat.contains("___")) {
            return mlFormat;
        }
        
        String[] parts = mlFormat.split("___");
        if (parts.length < 1) {
            return mlFormat;
        }
        
        // Get plant part (before "___")
        String plantPart = parts[0];
        
        // Remove parentheses content like (maize)
        if (plantPart.contains("(")) {
            plantPart = plantPart.substring(0, plantPart.indexOf("(")).trim();
        }
        
        // Replace underscores with spaces
        return plantPart.replace("_", " ").trim();
    }

    /**
     * Submit feedback for a prediction
     * POST /api/predictions/feedback
     * @param feedbackRequest Map containing predictionId, userId, isCorrect, feedbackText
     * @return Feedback response with success/failure status
     */
    @Operation(
        summary = "Submit feedback for a prediction",
        description = "Allows logged-in users to mark a prediction as correct or incorrect. " +
                      "Feedback is stored for admin review and model improvement."
    )
    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> feedbackRequest) {
        try {
            Integer predictionId = (Integer) feedbackRequest.get("predictionId");
            Integer userId = (Integer) feedbackRequest.get("userId");
            Boolean isCorrect = (Boolean) feedbackRequest.get("isCorrect");
            String feedbackText = (String) feedbackRequest.get("feedbackText");

            log.info("📝 Feedback received: predictionId={}, userId={}, isCorrect={}", 
                predictionId, userId, isCorrect);

            // Validate required fields
            if (predictionId == null || userId == null || isCorrect == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required fields");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            // Get prediction
            Prediction prediction = predictionService.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

            // Create feedback
            PredictionFeedback feedback = PredictionFeedback.builder()
                .prediction(prediction)
                .isCorrect(isCorrect)
                .comment(feedbackText != null ? feedbackText : "")
                .isApprovedFromAdmin(false) // Requires admin approval
                .createdAt(java.time.LocalDateTime.now())
                .build();

            // Save feedback
            feedback = feedbackService.submitFeedback(feedback);

            log.info("✅ Feedback saved: ID={}, isCorrect={}", 
                feedback.getFeedbackId(), isCorrect);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("feedbackId", feedback.getFeedbackId());
            response.put("message", "Feedback submitted successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("❌ Error submitting feedback: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all prediction logs (admin only)
     * GET /api/predictions/logs/all
     * @return List of all prediction logs
     */
    @GetMapping("/logs/all")
    public ResponseEntity<?> getAllLogs() {
        try {
            List<PredictionLog> logs = logRepository.findAll();
            
            // Convert to DTOs to avoid JSON serialization issues
            List<Map<String, Object>> logDTOs = logs.stream().map(logEntry -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", logEntry.getId());
                dto.put("predictionId", logEntry.getPrediction() != null ? logEntry.getPrediction().getId() : null);
                dto.put("adminUserId", null);
                dto.put("adminUserName", "System");
                dto.put("actionType", logEntry.getActionType());
                dto.put("timestamp", logEntry.getTimestamp() != null ? logEntry.getTimestamp().toString() : null);
                
                // Determine log level based on action type
                String level = "INFO";
                if (logEntry.getActionType() != null) {
                    if (logEntry.getActionType().contains("ERROR") || logEntry.getActionType().contains("FAIL")) {
                        level = "ERROR";
                    } else if (logEntry.getActionType().contains("WARNING") || logEntry.getActionType().contains("INVALID")) {
                        level = "WARNING";
                    }
                }
                dto.put("level", level);
                
                // Create message from action type
                String message = logEntry.getActionType() != null ? logEntry.getActionType() : "Unknown action";
                dto.put("message", message);
                
                return dto;
            }).collect(java.util.stream.Collectors.toList());
            
            log.info("📋 Retrieved {} prediction logs", logDTOs.size());
            return new ResponseEntity<>(logDTOs, HttpStatus.OK);
        } catch (Exception e) {
            log.error("❌ Error getting logs: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
