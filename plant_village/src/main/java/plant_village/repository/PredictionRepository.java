package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Prediction entity.
 * It serves as the data access layer between Java and MSSQL.
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Integer> {

    /**
     * Custom query method to fetch a specific user's predictions.
     * Results are sorted by creation date in descending order (Newest first).
     * @param userId The ID of the user
     * @return List of predictions belonging to the user
     */
    List<Prediction> findByUser_IdOrderByCreatedAtDesc(Integer userId);

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

