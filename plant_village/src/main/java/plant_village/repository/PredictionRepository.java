package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Integer> {

    // user prediction
    // The newest predictions will be listed at the top
    List<Prediction> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);

    Optional<Prediction> findByPredictionId(Integer predictionId);
    
    // admin control
    List<Prediction> findByIsValid(Boolean isValid);
}