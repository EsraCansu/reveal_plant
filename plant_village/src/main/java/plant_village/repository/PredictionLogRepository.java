package plant_village.repository;

import plant_village.model.PredictionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionLogRepository extends JpaRepository<PredictionLog, Integer> {

    // control and display
    // It fetches all logs of specific predictions, sorted from the newest record to the oldest.
    List<PredictionLog> findByPrediction_PredictionIdOrderByTimestampDesc(Integer predictionId);
    
    // admin activate control
    // Retrieves all log records made by a specified administrator.
    List<PredictionLog> findByAdminUser_UserId(Integer userId);
}
