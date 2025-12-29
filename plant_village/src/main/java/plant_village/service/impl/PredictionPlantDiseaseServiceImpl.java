package plant_village.service.impl;

import plant_village.model.*;
import plant_village.repository.*;
import plant_village.service.PredictionPlantDiseaseService;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PredictionPlantDiseaseServiceImpl implements PredictionPlantDiseaseService {
    
    @Autowired
    private PredictionRepository predictionRepository;
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Autowired
    private DiseaseRepository diseaseRepository;
    
    @Autowired
    private PredictionPlantRepository predictionPlantRepository;
    
    @Autowired
    private PredictionDiseaseRepository predictionDiseaseRepository;
    
    // ===== PLANT LINKING =====
    
    @Override
    public PredictionPlant linkPlantToPrediction(Integer predictionId, Integer plantId) {
        log.info("Bitki tahmin'e bağlanıyor - Prediction ID: {}, Plant ID: {}", predictionId, plantId);
        
        Prediction prediction = predictionRepository.findById(predictionId)
            .orElseThrow(() -> new ResourceNotFoundException("Tahmin bulunamadı - ID: " + predictionId));
        
        Plant plant = plantRepository.findById(plantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bitki bulunamadı - ID: " + plantId));
        
        PredictionPlant predictionPlant = PredictionPlant.builder()
            .predictionId(predictionId)
            .plantId(plantId)
            .prediction(prediction)
            .plant(plant)
            .build();
        
        PredictionPlant saved = predictionPlantRepository.save(predictionPlant);
        log.info("Bitki tahmin'e bağlandı - Prediction: {}, Plant: {}", predictionId, plantId);
        
        return saved;
    }
    
    @Override
    public Optional<PredictionPlant> getPredictionPlantByCompositeKey(Integer predictionId, Integer plantId) {
        log.info("PredictionPlant getiriliyor - Prediction ID: {}, Plant ID: {}", predictionId, plantId);
        Optional<PredictionPlant> predictionPlant = predictionPlantRepository.findByPredictionIdAndPlantId(predictionId, plantId);
        
        if (predictionPlant.isEmpty()) {
            log.warn("PredictionPlant bulunamadı - Prediction: {}, Plant: {}", predictionId, plantId);
            throw new ResourceNotFoundException("PredictionPlant bulunamadı - Prediction: " + predictionId + ", Plant: " + plantId);
        }
        
        return predictionPlant;
    }
    
    @Override
    public List<PredictionPlant> getPredictionPlantsByPredictionId(Integer predictionId) {
        log.info("Tahmin'in bitkileri getiriliyor - Prediction ID: {}", predictionId);
        
        List<PredictionPlant> plants = predictionPlantRepository.findByPredictionId(predictionId);
        
        if (plants.isEmpty()) {
            log.warn("Bu tahmin için bitki bulunamadı - Prediction ID: {}", predictionId);
            throw new ResourceNotFoundException("Bu tahmin için bitki bulunamadı - Prediction ID: " + predictionId);
        }
        
        return plants;
    }
    
    @Override
    public void deletePredictionPlant(Integer predictionId, Integer plantId) {
        log.info("PredictionPlant siliniyor - Prediction ID: {}, Plant ID: {}", predictionId, plantId);
        
        PredictionPlantId id = PredictionPlantId.builder()
            .predictionId(predictionId)
            .plantId(plantId)
            .build();
        
        if (!predictionPlantRepository.existsById(id)) {
            throw new ResourceNotFoundException("PredictionPlant bulunamadı - Prediction: " + predictionId + ", Plant: " + plantId);
        }
        
        predictionPlantRepository.deleteById(id);
        log.info("PredictionPlant silindi - Prediction: {}, Plant: {}", predictionId, plantId);
    }
    
    // ===== DISEASE LINKING =====
    
    @Override
    public PredictionDisease linkDiseaseToPrediction(Integer predictionId, Integer diseaseId, Boolean isHealthy) {
        log.info("Hastalık tahmin'e bağlanıyor - Prediction ID: {}, Disease ID: {}", predictionId, diseaseId);
        
        Prediction prediction = predictionRepository.findById(predictionId)
            .orElseThrow(() -> new ResourceNotFoundException("Tahmin bulunamadı - ID: " + predictionId));
        
        Disease disease = diseaseRepository.findById(diseaseId)
            .orElseThrow(() -> new ResourceNotFoundException("Hastalık bulunamadı - ID: " + diseaseId));
        
        PredictionDisease predictionDisease = PredictionDisease.builder()
            .predictionId(predictionId)
            .diseaseId(diseaseId)
            .prediction(prediction)
            .disease(disease)
            .isHealthy(isHealthy)
            .build();
        
        PredictionDisease saved = predictionDiseaseRepository.save(predictionDisease);
        log.info("Hastalık tahmin'e bağlandı - Prediction: {}, Disease: {}", predictionId, diseaseId);
        
        return saved;
    }
    
    @Override
    public Optional<PredictionDisease> getPredictionDiseaseByCompositeKey(Integer predictionId, Integer diseaseId) {
        log.info("PredictionDisease getiriliyor - Prediction ID: {}, Disease ID: {}", predictionId, diseaseId);
        Optional<PredictionDisease> predictionDisease = predictionDiseaseRepository.findByPredictionIdAndDiseaseId(predictionId, diseaseId);
        
        if (predictionDisease.isEmpty()) {
            log.warn("PredictionDisease bulunamadı - Prediction: {}, Disease: {}", predictionId, diseaseId);
            throw new ResourceNotFoundException("PredictionDisease bulunamadı - Prediction: " + predictionId + ", Disease: " + diseaseId);
        }
        
        return predictionDisease;
    }
    
    @Override
    public List<PredictionDisease> getPredictionDiseasesByPredictionId(Integer predictionId) {
        log.info("Tahmin'in hastalıkları getiriliyor - Prediction ID: {}", predictionId);
        
        List<PredictionDisease> diseases = predictionDiseaseRepository.findByPredictionId(predictionId);
        
        if (diseases.isEmpty()) {
            log.warn("Bu tahmin için hastalık bulunamadı - Prediction ID: {}", predictionId);
            throw new ResourceNotFoundException("Bu tahmin için hastalık bulunamadı - Prediction ID: " + predictionId);
        }
        
        return diseases;
    }
    
    @Override
    public PredictionDisease updatePredictionDisease(Integer predictionId, Integer diseaseId, Boolean isHealthy) {
        log.info("PredictionDisease güncelleniyor - Prediction: {}, Disease: {}, Is Healthy: {}", predictionId, diseaseId, isHealthy);
        
        PredictionDisease predictionDisease = predictionDiseaseRepository.findByPredictionIdAndDiseaseId(predictionId, diseaseId)
            .orElseThrow(() -> new ResourceNotFoundException("PredictionDisease bulunamadı - Prediction: " + predictionId + ", Disease: " + diseaseId));
        
        predictionDisease.setIsHealthy(isHealthy);
        
        PredictionDisease updated = predictionDiseaseRepository.save(predictionDisease);
        log.info("PredictionDisease güncellendi - Prediction: {}, Disease: {}", predictionId, diseaseId);
        
        return updated;
    }
    
    @Override
    public void deletePredictionDisease(Integer predictionId, Integer diseaseId) {
        log.info("PredictionDisease siliniyor - Prediction ID: {}, Disease ID: {}", predictionId, diseaseId);
        
        PredictionDiseaseId id = PredictionDiseaseId.builder()
            .predictionId(predictionId)
            .diseaseId(diseaseId)
            .build();
        
        if (!predictionDiseaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("PredictionDisease bulunamadı - Prediction: " + predictionId + ", Disease: " + diseaseId);
        }
        
        predictionDiseaseRepository.deleteById(id);
        log.info("PredictionDisease silindi - Prediction: {}, Disease: {}", predictionId, diseaseId);
    }
}
