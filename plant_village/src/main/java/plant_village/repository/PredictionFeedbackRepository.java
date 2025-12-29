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
     * Find all feedback with prediction joined - EAGER load
     * User accessed via prediction.user
     */
    @Query("SELECT pf FROM PredictionFeedback pf " +
           "LEFT JOIN FETCH pf.prediction p " +
           "LEFT JOIN FETCH p.user u " +
           "ORDER BY pf.createdAt DESC")
    List<PredictionFeedback> findAllWithUserAndPrediction();
    
    /**
     * Find feedback by prediction ID
     */
    List<PredictionFeedback> findByPrediction_Id(Integer predictionId);
    
    /**
     * Find approved feedback
     */
    List<PredictionFeedback> findByIsApprovedFromAdminTrue();
    
    /**
     * Find correct feedback
     */
    List<PredictionFeedback> findByIsCorrectTrue();
    
    /**
     * Count total correct feedback
     */
    long countByIsCorrectTrue();
    
    /**
     * Count total incorrect feedback
     */
    long countByIsCorrectFalse();
    
    /**
     * Count total approved feedback
     */
    long countByIsApprovedFromAdminTrue();
    
    /**
     * Count total pending (not approved) feedback
     */
    long countByIsApprovedFromAdminFalse();
    
    /**
     * Get feedback statistics using native query
     */
    @Query(value = "SELECT " +
                   "COUNT(*) as total, " +
                   "SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) as correct, " +
                   "SUM(CASE WHEN is_correct = 0 THEN 1 ELSE 0 END) as incorrect, " +
                   "SUM(CASE WHEN is_approved_from_admin = 1 THEN 1 ELSE 0 END) as approved, " +
                   "SUM(CASE WHEN is_approved_from_admin = 0 THEN 1 ELSE 0 END) as pending " +
                   "FROM Prediction_Feedback", 
           nativeQuery = true)
    List<Object[]> getFeedbackStatistics();
}
