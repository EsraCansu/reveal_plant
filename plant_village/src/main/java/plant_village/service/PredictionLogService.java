package plant_village.service;

import plant_village.model.PredictionLog;
import java.util.List;
import java.util.Optional;

public interface PredictionLogService {
    
    PredictionLog createLog(PredictionLog log);
    
    Optional<PredictionLog> getLogById(Integer id);
    
    List<PredictionLog> getLogsByPredictionId(Integer predictionId);
    
    List<PredictionLog> getAllLogs();
    
    void deleteLog(Integer id);
}
