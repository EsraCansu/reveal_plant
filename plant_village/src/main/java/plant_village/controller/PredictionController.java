package plant_village.controller;

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

@RestController
@RequestMapping("/api/predictions")
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
            
            // Create feedback entity
            PredictionFeedback feedback = PredictionFeedback.builder()
                .predictionId(predictionId)
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
}
