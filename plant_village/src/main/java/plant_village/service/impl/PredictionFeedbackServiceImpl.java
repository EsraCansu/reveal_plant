package plant_village.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plant_village.exception.ResourceNotFoundException;
import plant_village.model.PredictionFeedback;
import plant_village.repository.PredictionFeedbackRepository;
import plant_village.repository.PredictionRepository;
import plant_village.service.PredictionFeedbackService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for Prediction Feedback operations.
 * Handles feedback submission, retrieval, and statistics calculation.
 * 
 * STEP 5: Feedback - User feedback on predictions
 */
@Service
@Slf4j
public class PredictionFeedbackServiceImpl implements PredictionFeedbackService {
    
    @Autowired
    private PredictionFeedbackRepository feedbackRepository;
    
    @Autowired
    private PredictionRepository predictionRepository;
    
    /**
     * Submit feedback for a prediction
     * Validates prediction exists and creates feedback entry
     * STEP 5: Feedback - Create feedback
     */
    @Override
    public PredictionFeedback submitFeedback(PredictionFeedback feedback) {
        log.info("Submitting feedback for prediction");
        
        // feedback object should contain prediction reference
        // Verify prediction exists
        if (feedback.getPrediction() == null || feedback.getPrediction().getId() == null) {
            log.warn("Prediction not found for feedback");
            throw new ResourceNotFoundException("Tahmin bulunamadı");
        }
        
        // Verify prediction exists in DB
        if (!predictionRepository.existsById(feedback.getPrediction().getId())) {
            log.warn("Prediction not found for feedback: {}", feedback.getPrediction().getId());
            throw new ResourceNotFoundException(
                "Tahmin bulunamadı - ID: " + feedback.getPrediction().getId()
            );
        }
        
        // Set creation timestamp if not provided
        if (feedback.getCreatedAt() == null) {
            feedback.setCreatedAt(LocalDateTime.now());
        }
        
        // Set default values
        if (feedback.getIsApprovedFromAdmin() == null) {
            feedback.setIsApprovedFromAdmin(false);
        }
        if (feedback.getImageAddedToDb() == null) {
            feedback.setImageAddedToDb(false);
        }
        
        PredictionFeedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback submitted successfully for prediction ID: {} (Feedback ID: {})",
            feedback.getPrediction().getId(), savedFeedback.getFeedbackId());
        
        return savedFeedback;
    }
    
    /**
     * Get all feedback ordered by creation date
     * STEP 5: Feedback - Retrieve all feedback (admin)
     */
    @Override
    public List<PredictionFeedback> getAllFeedback() {
        log.info("Fetching all feedback with user and prediction details");
        
        // Use custom query with JOIN FETCH to eagerly load user and prediction
        List<PredictionFeedback> feedbackList = feedbackRepository.findAllWithUserAndPrediction();
        
        log.info("Retrieved {} feedback entries with joined relations", feedbackList.size());
        
        return feedbackList;
    }
    
    /**
     * Get feedback for a specific prediction
     * STEP 5: Feedback - Get feedback for prediction
     */
    @Override
    public List<PredictionFeedback> getFeedbackByPredictionId(Integer predictionId) {
        log.info("Fetching feedback for prediction ID: {}", predictionId);
        
        // Verify prediction exists
        if (!predictionRepository.existsById(predictionId)) {
            log.warn("Prediction not found: {}", predictionId);
            throw new ResourceNotFoundException(
                "Tahmin bulunamadı - ID: " + predictionId
            );
        }
        
        List<PredictionFeedback> feedbackList = feedbackRepository.findAll().stream()
            .filter(f -> f.getPrediction() != null && f.getPrediction().getId().equals(predictionId))
            .toList();
        
        log.info("Retrieved {} feedback entries for prediction ID: {}", feedbackList.size(), predictionId);
        
        return feedbackList;
    }
    
    /**
     * Get feedback by user
     * STEP 5: Feedback - Get user's feedback history
     */
    @Override
    public List<PredictionFeedback> getFeedbackByUserId(Integer userId) {
        log.info("Fetching feedback for user ID: {}", userId);
        
        List<PredictionFeedback> feedbackList = feedbackRepository.findAll().stream()
            .filter(f -> f.getPrediction() != null && f.getPrediction().getUser() != null 
                && f.getPrediction().getUser().getId().equals(userId))
            .toList();
        
        log.info("Retrieved {} feedback entries for user ID: {}", feedbackList.size(), userId);
        
        return feedbackList;
    }
    
    /**
     * Get feedback statistics (total, correct, incorrect, accuracy)
     * STEP 5: Feedback - Get feedback statistics
     */
    @Override
    public Map<String, Object> getFeedbackStatistics() {
        log.info("Calculating feedback statistics");
        
        List<PredictionFeedback> allFeedback = feedbackRepository.findAll();
        
        if (allFeedback.isEmpty()) {
            log.info("No feedback found for statistics calculation");
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalFeedback", 0);
            emptyStats.put("correctPredictions", 0);
            emptyStats.put("incorrectPredictions", 0);
            emptyStats.put("accuracy", 0.0);
            emptyStats.put("approvedFeedback", 0);
            emptyStats.put("pendingFeedback", 0);
            return emptyStats;
        }
        
        long totalFeedback = allFeedback.size();
        long approvedFeedback = allFeedback.stream()
            .filter(f -> f.getIsApprovedFromAdmin() != null && f.getIsApprovedFromAdmin())
            .count();
        long pendingFeedback = allFeedback.stream()
            .filter(f -> f.getIsApprovedFromAdmin() == null || !f.getIsApprovedFromAdmin())
            .count();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalFeedback", totalFeedback);
        statistics.put("approvedFeedback", approvedFeedback);
        statistics.put("pendingFeedback", pendingFeedback);
        
        log.info("Feedback statistics calculated - Total: {}, Approved: {}, Pending: {}",
            totalFeedback, approvedFeedback, pendingFeedback);
        
        return statistics;
    }
    
    /**
     * Approve feedback by admin
     * STEP 5: Feedback - Admin approval
     * If isCorrect=true, save image to DB for ML training
     */
    @Override
    public PredictionFeedback approveFeedback(Integer feedbackId) {
        log.info("Approving feedback with ID: {}", feedbackId);
        
        return feedbackRepository.findById(feedbackId)
            .map(feedback -> {
                feedback.setIsApprovedFromAdmin(true);
                feedback.setIsApproved(true);  // isApprovedFromAdmin 1 (true) oluyorsa isApproved da 1 (true) olsun
                
                // If feedback is correct, mark image as added to DB for ML training
                if (Boolean.TRUE.equals(feedback.getIsCorrect())) {
                    feedback.setImageAddedToDb(true);
                    log.info("✅ Correct prediction approved - Image marked for ML training. Feedback ID: {}", feedbackId);
                } else {
                    // Incorrect predictions don't need to save image
                    feedback.setImageAddedToDb(false);
                    log.info("❌ Incorrect prediction approved - Image NOT added to training. Feedback ID: {}", feedbackId);
                }
                
                PredictionFeedback approved = feedbackRepository.save(feedback);
                log.info("Feedback approved successfully - ID: {}, isCorrect: {}, imageAddedToDb: {}", 
                    feedbackId, feedback.getIsCorrect(), feedback.getImageAddedToDb());
                return approved;
            })
            .orElseThrow(() -> {
                log.error("Feedback not found with ID: {}", feedbackId);
                return new RuntimeException("Feedback not found with ID: " + feedbackId);
            });
    }
    
    /**
     * Process pending feedback (add images that weren't added yet)
     * STEP 5: Feedback - Process pending feedback
     */
    @Override
    public void processPendingFeedback() {
        log.info("Processing pending feedback");
        
        List<PredictionFeedback> pendingFeedback = feedbackRepository.findAll().stream()
            .filter(f -> f.getImageAddedToDb() != null && !f.getImageAddedToDb())
            .toList();
        
        if (pendingFeedback.isEmpty()) {
            log.info("No pending feedback to process");
            return;
        }
        
        // Process each pending feedback
        for (PredictionFeedback feedback : pendingFeedback) {
            try {
                // Only process feedback with comments/images
                if (feedback.getComment() != null) {
                    
                    // Mark as processed (image added to db)
                    feedback.setImageAddedToDb(true);
                    feedbackRepository.save(feedback);
                    
                    log.info("Processed feedback image - Feedback ID: {}", feedback.getFeedbackId());
                }
            } catch (Exception e) {
                log.error("Error processing feedback ID: {} - {}", feedback.getFeedbackId(), e.getMessage());
            }
        }
        
        log.info("Processed {} pending feedback entries", pendingFeedback.size());
    }
}
