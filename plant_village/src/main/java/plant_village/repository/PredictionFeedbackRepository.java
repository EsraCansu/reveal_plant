package plant_village.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import plant_village.model.PredictionFeedback;

import java.util.List;

/**
 * Repository interface for PredictionFeedback entity.
 * Provides database operations for prediction feedback management.
 */
@Repository
public interface PredictionFeedbackRepository extends JpaRepository<PredictionFeedback, Integer> {
    
    /**
     * Find all feedback ordered by creation date (newest first)
     */
    List<PredictionFeedback> findAllByOrderByCreatedAtDesc();
    
    /**
     * Find feedback by prediction ID
     */
    List<PredictionFeedback> findByPredictionId(Integer predictionId);
    
    /**
     * Find feedback by user
     */
    List<PredictionFeedback> findByUser_Id(Integer userId);
    
    /**
     * Find correct predictions
     */
    List<PredictionFeedback> findByIsCorrectTrue();
    
    /**
     * Count total correct predictions
     */
    long countByIsCorrectTrue();
    
    /**
     * Count total incorrect predictions
     */
    long countByIsCorrectFalse();
    
    /**
     * Get feedback statistics using native query
     */
    @Query(value = "SELECT prediction_type, " +
                   "COUNT(*) as total, " +
                   "SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) as correct, " +
                   "SUM(CASE WHEN is_correct = 0 THEN 1 ELSE 0 END) as incorrect, " +
                   "CAST(SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS DECIMAL(5,2)) as accuracy " +
                   "FROM Prediction_Feedback " +
                   "GROUP BY prediction_type", 
           nativeQuery = true)
    List<Object[]> getFeedbackStatistics();
}
