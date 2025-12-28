package plant_village.service.impl;

import plant_village.model.PredictionLog;
import plant_village.repository.PredictionLogRepository;
import plant_village.service.PredictionLogService;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PredictionLogServiceImpl implements PredictionLogService {
    
    @Autowired
    private PredictionLogRepository predictionLogRepository;
    
    @Override
    public PredictionLog createLog(PredictionLog predictionLog) {
        log.info("Yeni log kaydı oluşturuluyor - Prediction ID: {}, Action: {}", predictionLog.getPrediction().getId(), predictionLog.getActionType());
        
        if (predictionLog.getTimestamp() == null) {
            predictionLog.setTimestamp(LocalDateTime.now());
        }
        
        return predictionLogRepository.save(predictionLog);
    }
    
    @Override
    public Optional<PredictionLog> getLogById(Integer id) {
        log.info("Log kaydı getiriliyor - ID: {}", id);
        Optional<PredictionLog> predictionLog = predictionLogRepository.findById(id);
        if (predictionLog.isEmpty()) {
            log.warn("Log kaydı bulunamadı - ID: {}", id);
            throw new ResourceNotFoundException("Log kaydı bulunamadı - ID: " + id);
        }
        return predictionLog;
    }
    
    @Override
    public List<PredictionLog> getLogsByPredictionId(Integer predictionId) {
        log.info("Log kayıtları getiriliyor - Prediction ID: {}", predictionId);
        List<PredictionLog> logs = predictionLogRepository.findByPrediction_Id(predictionId);
        
        if (logs.isEmpty()) {
            log.warn("Log kaydı bulunamadı - Prediction ID: {}", predictionId);
            throw new ResourceNotFoundException("Bu tahmin için log kaydı bulunamadı - Prediction ID: " + predictionId);
        }
        
        return logs;
    }
    
    @Override
    public List<PredictionLog> getAllLogs() {
        log.info("Tüm log kayıtları listeleniyor");
        return predictionLogRepository.findAll();
    }
    
    @Override
    public void deleteLog(Integer id) {
        log.info("Log kaydı siliniyor - ID: {}", id);
        if (!predictionLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Log kaydı bulunamadı - ID: " + id);
        }
        predictionLogRepository.deleteById(id);
        log.info("Log kaydı silindi - ID: {}", id);
    }
}