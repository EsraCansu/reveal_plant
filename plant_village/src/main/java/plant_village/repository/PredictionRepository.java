package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Prediction entity.
 * It serves as the data access layer between Java and MSSQL.
 * 
 * Supports:
 * - LIFO Stack ordering for prediction history
 * - %50 Rule validation filtering
 * - Efficient Tree-based data retrieval
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Integer> {

    /**
     * Fetch all predictions for a specific user.
     * 
     * @param userId The ID of the user
     * @return List of predictions belonging to the user
     */
    List<Prediction> findByUser_Id(Integer userId);

    /**
     * Custom query method to fetch a specific user's predictions.
     * Results are sorted by creation date in descending order (Newest first - LIFO Stack).
     * 
     * @param userId The ID of the user
     * @return List of predictions belonging to the user in reverse chronological order
     */
    List<Prediction> findByUser_IdOrderByCreateAtDesc(Integer userId);

    /**
     * Fetch user predictions with limit (for pagination/stack view).
     * Most recent predictions first (LIFO - Last In First Out).
     * 
     * @param userId The ID of the user
     * @param limit Maximum number of predictions to return
     * @return Limited list of user's predictions
     */
    @Query(value = 
        "SELECT TOP :limit * FROM Prediction WHERE user_id = :userId ORDER BY create_at DESC",
        nativeQuery = true)
    List<Prediction> findByUserIdWithLimit(
        @Param("userId") Integer userId,
        @Param("limit") Integer limit
    );

    /**
     * Fetch only valid predictions (confidence >= 50%).
     * These predictions passed the %50 Rule validation.
     * 
     * @param userId The ID of the user
     * @return List of valid predictions ordered by creation date (newest first)
     */
    List<Prediction> findByUser_IdAndIsValidTrueOrderByCreateAtDesc(Integer userId);

    /**
     * Fetch only invalid predictions (confidence < 50%).
     * These predictions failed the %50 Rule validation.
     * 
     * @param userId The ID of the user
     * @return List of invalid predictions ordered by creation date (newest first)
     */
    List<Prediction> findByUser_IdAndIsValidFalseOrderByCreateAtDesc(Integer userId);

    /**
     * Administrative query method.
     * Filters predictions based on their validation status.
     * @param isValid Boolean flag for validation
     * @return List of validated or invalidated predictions
     */
    List<Prediction> findByIsValid(Boolean isValid);
    
    // Note: Standard CRUD methods like save(), findById(), and delete() 
    // are automatically inherited from JpaRepository.
}