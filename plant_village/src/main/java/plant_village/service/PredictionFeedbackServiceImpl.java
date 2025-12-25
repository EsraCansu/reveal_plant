package plant_village.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plant_village.model.*;
import plant_village.repository.*;

import java.util.*;

/**
 * Service implementation for PredictionFeedback operations.
 * Handles feedback submission and admin approval for training data.
 */
@Service
public class PredictionFeedbackServiceImpl implements PredictionFeedbackService {
    
    @Autowired
    private PredictionFeedbackRepository feedbackRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Submit feedback for a prediction
     */
    @Override
    @Transactional
    public PredictionFeedback submitFeedback(PredictionFeedback feedback) {
        // Save the feedback
        PredictionFeedback savedFeedback = feedbackRepository.save(feedback);
        return savedFeedback;
    }
    
    @Override
    public List<PredictionFeedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Override
    public List<PredictionFeedback> getFeedbackByPredictionId(Integer predictionId) {
        return feedbackRepository.findByPredictionId(predictionId);
    }
    
    @Override
    public List<PredictionFeedback> getFeedbackByUserId(Integer userId) {
        return feedbackRepository.findByUser_Id(userId);
    }
    
    @Override
    public Map<String, Object> getFeedbackStatistics() {
        long total = feedbackRepository.count();
        long correct = feedbackRepository.countByIsCorrectTrue();
        long incorrect = feedbackRepository.countByIsCorrectFalse();
        double accuracy = total > 0 ? (correct * 100.0 / total) : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("correct", correct);
        stats.put("incorrect", incorrect);
        stats.put("accuracy", String.format("%.1f", accuracy));
        
        return stats;
    }
    
    @Override
    @Transactional
    public void processPendingFeedback() {
        // In new schema, feedback processing is handled separately through PredictionLog
        System.out.println("Feedback processing managed through PredictionLog entity");
    }
}
