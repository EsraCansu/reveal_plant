package plant_village.repository;

import plant_village.model.PredictionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionLogRepository extends JpaRepository<PredictionLog, Integer> {

    // control and display - fetch logs of specific prediction, newest first
    List<PredictionLog> findByPrediction_IdOrderByTimestampDesc(Integer predictionId);
    
    // Retrieves all log records for a specific user
    List<PredictionLog> findByUser_IdOrderByTimestampDesc(Integer userId);
}
