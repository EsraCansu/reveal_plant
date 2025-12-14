package plant_village.service.impl;

import plant_village.model.*;
import plant_village.model.dto.FastAPIResponse;
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

    /**
     * Real-time prediction API with FastAPI integration
     */
    @Override
    public Prediction predictPlantDisease(Long userId, Long plantId, String imageBase64, String description) {
        try {
            log.info("Starting prediction process for user: {}, plant: {}", userId, plantId);

            // 1. Get FastAPI prediction
            FastAPIResponse fastApiResponse = fastAPIClientService.predictDisease(
                    plantId, 
                    imageBase64, 
                    description
            );

            if (fastApiResponse == null || !"success".equalsIgnoreCase(fastApiResponse.getStatus())) {
                throw new RuntimeException("FastAPI prediction failed: " + 
                    (fastApiResponse != null ? fastApiResponse.getMessage() : "No response"));
            }

            log.debug("FastAPI response: {}", fastApiResponse.getTopPrediction());

            // 2. Create Prediction record
            Prediction prediction = new Prediction();
            prediction.setCreatedAt(LocalDateTime.now());
            prediction.setIsValid(true);
            prediction.setConfidence(fastApiResponse.getTopConfidence());

            // Get User
            User user = userRepository.findById(userId.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            prediction.setUser(user);

            // 3. Link Plant
            Plant plant = plantRepository.findById(plantId.intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Plant not found: " + plantId));
            
            PredictionPlant predictionPlant = new PredictionPlant();
            predictionPlant.setPlant(plant);
            prediction.setPredictionPlant(predictionPlant);

            // Save prediction first
            Prediction savedPrediction = predictionRepository.save(prediction);
            predictionPlant.setPrediction(savedPrediction);
            predictionPlantRepository.save(predictionPlant);

            // 4. Link Diseases from FastAPI predictions
            List<PredictionDisease> predictionDiseases = new ArrayList<>();
            
            if (fastApiResponse.getPredictions() != null && !fastApiResponse.getPredictions().isEmpty()) {
                // Get top 3 disease predictions
                List<String> topDiseases = fastApiResponse.getPredictions().entrySet().stream()
                        .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                        .limit(3)
                        .map(e -> e.getKey())
                        .collect(Collectors.toList());

                for (String diseaseName : topDiseases) {
                    try {
                        Disease disease = diseaseRepository.findByNameIgnoreCase(diseaseName)
                                .orElseGet(() -> {
                                    // Create new disease if not found
                                    Disease newDisease = new Disease();
                                    newDisease.setName(diseaseName);
                                    newDisease.setDescription("Auto-discovered disease from ML model");
                                    newDisease.setRecommendation(fastApiResponse.getRecommendedAction());
                                    return diseaseRepository.save(newDisease);
                                });

                        PredictionDisease predictionDisease = new PredictionDisease();
                        predictionDisease.setPrediction(savedPrediction);
                        predictionDisease.setDisease(disease);
                        predictionDisease.setConfidence(fastApiResponse.getPredictions().get(diseaseName));

                        predictionDiseases.add(predictionDisease);
                    } catch (Exception e) {
                        log.warn("Error linking disease: {}", diseaseName, e);
                    }
                }

                // Save all disease links
                predictionDiseaseRepository.saveAll(predictionDiseases);
                savedPrediction.setPredictionDiseases(predictionDiseases);
            }

            // 5. Update and return prediction with all relations
            log.info("Prediction completed successfully. ID: {}, Disease: {}, Confidence: {}",
                    savedPrediction.getId(), 
                    fastApiResponse.getTopPrediction(),
                    fastApiResponse.getTopConfidence());

            return savedPrediction;

        } catch (Exception e) {
            log.error("Error in predictPlantDisease", e);
            throw new RuntimeException("Prediction failed: " + e.getMessage(), e);
        }
    }
}