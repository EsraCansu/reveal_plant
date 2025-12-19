package plant_village.service;

import plant_village.model.*;
import plant_village.model.dto.FastAPIResponse;
import plant_village.repository.*;
import plant_village.service.*;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    @Transactional
    public Prediction createPrediction(Prediction prediction) {
        prediction.setCreatedAt(LocalDateTime.now());
        prediction.setIsValid(true);
        return predictionRepository.save(prediction);
    }

    @Override
    @Transactional
    public Prediction predictPlantDisease(Integer userId, Integer plantId, String imageBase64, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Integer fastApiPlantId = plantId != null ? plantId : 0;
        FastAPIResponse apiResponse = fastAPIClientService.predictDisease(fastApiPlantId, imageBase64, description);

        // Sonra Builder içinde kullanıyoruz
        Prediction prediction = Prediction.builder()
                .user(user)
                .confidence(apiResponse.getTopConfidence()) // Artık tertemiz bir Integer
                .createdAt(LocalDateTime.now())
                .predictionType(plantId == null ? "PLANT_PREDICTION" : "DISEASE_PREDICTION")
                .isValid(true)
                .uploadedImageUrl("uploads/" + LocalDateTime.now().getNano() + ".jpg")
                .build();

        Prediction savedPrediction = predictionRepository.save(prediction);

        boolean isPlantRequest = (plantId == null); 
        boolean isHealthy = apiResponse.getTopPrediction().toLowerCase().contains("healthy");

        if (isPlantRequest || isHealthy) {
            plantRepository.findByPlantName(apiResponse.getTopPrediction())
                .ifPresent(plant -> {
                    PredictionPlant pp = PredictionPlant.builder()
                            .prediction(savedPrediction)
                            .plant(plant)
                            .build();
                    predictionPlantRepository.save(pp);
                });
            log.info("Sonuç Sağlıklı veya Bitki Tahmini: PredictionPlant tablosuna kaydedildi.");
        } 
        else {
            diseaseRepository.findByNameIgnoreCase(apiResponse.getTopPrediction())
                .ifPresent(disease -> {
                    PredictionDisease pd = PredictionDisease.builder()
                            .prediction(savedPrediction)
                            .disease(disease)
                            .isHealthy(false) 
                            .build();
                    predictionDiseaseRepository.save(pd);
                });
            log.info("Sonuç Hastalıklı: PredictionDisease tablosuna kaydedildi.");
        }

        return savedPrediction;
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
    @Transactional
    public Prediction updatePrediction(Integer predictionId, Prediction updatedPrediction, User adminUser) {
        Prediction existingPrediction = predictionRepository.findById(predictionId)
                                         .orElseThrow(() -> new ResourceNotFoundException("Tahmin kaydı bulunamadı."));
        
        String oldValue = "Confidence: " + existingPrediction.getConfidence() + ", Valid: " + existingPrediction.getIsValid(); 

        existingPrediction.setConfidence(updatedPrediction.getConfidence());
        existingPrediction.setIsValid(updatedPrediction.getIsValid());

        Prediction savedPrediction = predictionRepository.save(existingPrediction);

        PredictionLog logEntry = PredictionLog.builder()
                .prediction(savedPrediction)
                .adminUser(adminUser)
                .actionType("UPDATE")
                .timestamp(LocalDateTime.now())
                .oldValue(oldValue)
                .newValue("Confidence: " + savedPrediction.getConfidence() + ", Valid: " + savedPrediction.getIsValid())
                .build();

        predictionLogRepository.save(logEntry);

        return savedPrediction;
    }
}