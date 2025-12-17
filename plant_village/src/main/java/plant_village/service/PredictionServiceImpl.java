package plant_village.service.impl;

import plant_village.model.*;
import plant_village.model.dto.FastAPIResponse;
import plant_village.model.dto.DiseasePrediction;
import plant_village.repository.*;
import plant_village.service.PredictionService;
import plant_village.service.FastAPIClientService;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final PredictionLogRepository predictionLogRepository; 
    private final PredictionDiseaseRepository predictionDiseaseRepository;
    private final PredictionPlantRepository predictionPlantRepository;
    private final FastAPIClientService fastAPIClientService;
    private final PlantRepository plantRepository;
    private final DiseaseRepository diseaseRepository;
    private final UserRepository userRepository;

    @Autowired
    public PredictionServiceImpl(PredictionRepository predictionRepository, 
                                PredictionLogRepository predictionLogRepository,
                                PredictionDiseaseRepository predictionDiseaseRepository,
                                PredictionPlantRepository predictionPlantRepository,
                                FastAPIClientService fastAPIClientService,
                                PlantRepository plantRepository,
                                DiseaseRepository diseaseRepository,
                                UserRepository userRepository) {
        this.predictionRepository = predictionRepository;
        this.predictionLogRepository = predictionLogRepository;
        this.predictionDiseaseRepository = predictionDiseaseRepository;
        this.predictionPlantRepository = predictionPlantRepository;
        this.fastAPIClientService = fastAPIClientService;
        this.plantRepository = plantRepository;
        this.diseaseRepository = diseaseRepository;
        this.userRepository = userRepository;
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
        return predictionRepository.findByUser_IdOrderByCreatedAtDesc(userId);
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
        log.setActionType("UPDATE");
        log.setTimestamp(LocalDateTime.now());
        log.setOldValue(oldValue);
        log.setNewValue(savedPrediction.toString()); 

        predictionLogRepository.save(log); // record the log

        return savedPrediction;
    }

    /**
     * Real-time prediction API with FastAPI integration
     * TODO: Implement full prediction logic
     */
    @Override
    public Prediction predictPlantDisease(Integer userId, Integer plantId, String imageBase64, String description) {
        // TODO: Implement full prediction flow
        // For now, return stub implementation to allow compilation
        Prediction prediction = new Prediction();
        prediction.setCreatedAt(LocalDateTime.now());
        prediction.setIsValid(true);
        prediction.setConfidence(0);
        return predictionRepository.save(prediction);
    }
}