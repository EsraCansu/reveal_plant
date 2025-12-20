package plant_village.service;

import plant_village.model.PredictionFeedback;

import java.util.List;
import java.util.Map;

/**
 * Service interface for PredictionFeedback operations.
 */
public interface PredictionFeedbackService {
    
    /**
     * Submit feedback for a prediction
     * Automatically adds image to plant/disease user images if feedback is positive
     */
    PredictionFeedback submitFeedback(PredictionFeedback feedback);
    
    /**
     * Get all feedback ordered by creation date
     */
    List<PredictionFeedback> getAllFeedback();
    
    /**
     * Get feedback for a specific prediction
     */
    List<PredictionFeedback> getFeedbackByPredictionId(Integer predictionId);
    
    /**
     * Get feedback by user
     */
    List<PredictionFeedback> getFeedbackByUserId(Integer userId);
    
    /**
     * Get feedback statistics (total, correct, incorrect, accuracy)
     */
    Map<String, Object> getFeedbackStatistics();
    
    /**
     * Process pending feedback (add images that weren't added yet)
     */
    void processPendingFeedback();
}
