package plant_village.service.impl;

import plant_village.model.Prediction;
import plant_village.model.PredictionLog;
import plant_village.model.User;
import plant_village.repository.PredictionRepository;
import plant_village.repository.PredictionLogRepository;
import plant_village.repository.PredictionDiseaseRepository;
import plant_village.repository.PredictionPlantRepository;
import plant_village.service.PredictionService;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final PredictionLogRepository predictionLogRepository; 
    private final PredictionDiseaseRepository predictionDiseaseRepository;
    private final PredictionPlantRepository predictionPlantRepository;
    

    @Autowired
    public PredictionServiceImpl(PredictionRepository predictionRepository, 
                                PredictionLogRepository predictionLogRepository,
                                PredictionDiseaseRepository predictionDiseaseRepository,
                                PredictionPlantRepository predictionPlantRepository) {
        this.predictionRepository = predictionRepository;
        this.predictionLogRepository = predictionLogRepository;
        this.predictionDiseaseRepository = predictionDiseaseRepository;
        this.predictionPlantRepository = predictionPlantRepository;
    }

    @Override
    public Prediction createPrediction(Prediction prediction) {
        prediction.setCreatedAt(LocalDateTime.now());
        prediction.setIsValid(true); // new predictions may change for default prediction
        
        // **NOT:** Burası ML model çıktısını işleme, Plant/Disease ilişkilerini kurma 
        // mantığının yazılacağı yerdir.
        
        return predictionRepository.save(prediction);
    }

    @Override
    public List<Prediction> getPredictionHistory(Integer userId) {
        return predictionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public List<Prediction> getInvalidPredictionsForReview() {
        return predictionRepository.findByIsValid(false);
    }
    
    @Override
    public Optional<Prediction> findById(Integer predictionId) {
        return predictionRepository.findById(predictionId);
    }
    
    @Override
    public Prediction updatePrediction(Integer predictionId, Prediction updatedPrediction, User adminUser) {
        Prediction existingPrediction = predictionRepository.findById(predictionId)
                                         .orElseThrow(() -> new ResourceNotFoundException("Tahmin kaydı bulunamadı."));
        
        // record the old values for second log
        String oldValue = existingPrediction.toString(); 

        existingPrediction.setConfidence(updatedPrediction.getConfidence());
        existingPrediction.setIsValid(updatedPrediction.getIsValid());

        Prediction savedPrediction = predictionRepository.save(existingPrediction);

        PredictionLog log = new PredictionLog();
        log.setPrediction(savedPrediction);
        log.setAdminUser(adminUser); 
        log.setActionType(1);
        log.setTimestamp(LocalDateTime.now());
        log.setOldValue(oldValue);
        log.setNewValue(savedPrediction.toString()); 

        predictionLogRepository.save(log); // record the log

        return savedPrediction;
    }
}