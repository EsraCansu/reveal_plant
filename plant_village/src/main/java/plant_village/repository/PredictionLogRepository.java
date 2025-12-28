package plant_village.repository;

import plant_village.model.PredictionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionLogRepository extends JpaRepository<PredictionLog, Integer> {

    // Fetch logs of specific prediction
    List<PredictionLog> findByPrediction_Id(Integer predictionId);

    // Fetch logs of specific prediction, newest first
    List<PredictionLog> findByPrediction_IdOrderByTimestampDesc(Integer predictionId);
    
    // Fetch logs for a specific user's predictions (using user_id from prediction table)
    @Query("SELECT pl FROM PredictionLog pl WHERE pl.prediction.user.id = :userId ORDER BY pl.timestamp DESC")
    List<PredictionLog> findByUserId(@Param("userId") Integer userId);
}
