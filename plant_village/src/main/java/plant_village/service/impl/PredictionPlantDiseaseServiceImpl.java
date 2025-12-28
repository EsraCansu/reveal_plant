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
    public PredictionPlant linkPlantToPrediction(Integer predictionId, Integer plantId, Float confidence) {
        log.info("Bitki tahmin'e bağlanıyor - Prediction ID: {}, Plant ID: {}", predictionId, plantId);
        
        Prediction prediction = predictionRepository.findById(predictionId)
            .orElseThrow(() -> new ResourceNotFoundException("Tahmin bulunamadı - ID: " + predictionId));
        
        Plant plant = plantRepository.findById(plantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bitki bulunamadı - ID: " + plantId));
        
        PredictionPlant predictionPlant = PredictionPlant.builder()
            .prediction(prediction)
            .plant(plant)
            .matchConfidence(confidence != null ? confidence.doubleValue() : null)
            .build();
        
        PredictionPlant saved = predictionPlantRepository.save(predictionPlant);
        log.info("Bitki tahmin'e bağlandı - PredictionPlant ID: {}", saved.getId());
        
        return saved;
    }
    
    @Override
    public Optional<PredictionPlant> getPredictionPlantById(Integer id) {
        log.info("PredictionPlant getiriliyor - ID: {}", id);
        Optional<PredictionPlant> predictionPlant = predictionPlantRepository.findById(id);
        
        if (predictionPlant.isEmpty()) {
            log.warn("PredictionPlant bulunamadı - ID: {}", id);
            throw new ResourceNotFoundException("PredictionPlant bulunamadı - ID: " + id);
        }
        
        return predictionPlant;
    }
    
    @Override
    public List<PredictionPlant> getPredictionPlantsByPredictionId(Integer predictionId) {
        log.info("Tahmin'in bitkileri getiriliyor - Prediction ID: {}", predictionId);
        
        List<PredictionPlant> plants = predictionPlantRepository.findByPrediction_Id(predictionId);
        
        if (plants.isEmpty()) {
            log.warn("Bu tahmin için bitki bulunamadı - Prediction ID: {}", predictionId);
            throw new ResourceNotFoundException("Bu tahmin için bitki bulunamadı - Prediction ID: " + predictionId);
        }
        
        return plants;
    }
    
    @Override
    public void deletePredictionPlant(Integer id) {
        log.info("PredictionPlant siliniyor - ID: {}", id);
        
        if (!predictionPlantRepository.existsById(id)) {
            throw new ResourceNotFoundException("PredictionPlant bulunamadı - ID: " + id);
        }
        
        predictionPlantRepository.deleteById(id);
        log.info("PredictionPlant silindi - ID: {}", id);
    }
    
    // ===== DISEASE LINKING =====
    
    @Override
    public PredictionDisease linkDiseaseToPrediction(Integer predictionId, Integer diseaseId, Boolean isHealthy, Float confidence) {
        log.info("Hastalık tahmin'e bağlanıyor - Prediction ID: {}, Disease ID: {}", predictionId, diseaseId);
        
        Prediction prediction = predictionRepository.findById(predictionId)
            .orElseThrow(() -> new ResourceNotFoundException("Tahmin bulunamadı - ID: " + predictionId));
        
        Disease disease = diseaseRepository.findById(diseaseId)
            .orElseThrow(() -> new ResourceNotFoundException("Hastalık bulunamadı - ID: " + diseaseId));
        
        PredictionDisease predictionDisease = PredictionDisease.builder()
            .prediction(prediction)
            .disease(disease)
            .isHealthy(isHealthy)
            .matchConfidence(confidence != null ? confidence.doubleValue() : null)
            .build();
        
        PredictionDisease saved = predictionDiseaseRepository.save(predictionDisease);
        log.info("Hastalık tahmin'e bağlandı - PredictionDisease ID: {}", saved.getId());
        
        return saved;
    }
    
    @Override
    public Optional<PredictionDisease> getPredictionDiseaseById(Integer id) {
        log.info("PredictionDisease getiriliyor - ID: {}", id);
        Optional<PredictionDisease> predictionDisease = predictionDiseaseRepository.findById(id);
        
        if (predictionDisease.isEmpty()) {
            log.warn("PredictionDisease bulunamadı - ID: {}", id);
            throw new ResourceNotFoundException("PredictionDisease bulunamadı - ID: " + id);
        }
        
        return predictionDisease;
    }
    
    @Override
    public List<PredictionDisease> getPredictionDiseasesByPredictionId(Integer predictionId) {
        log.info("Tahmin'in hastalıkları getiriliyor - Prediction ID: {}", predictionId);
        
        List<PredictionDisease> diseases = predictionDiseaseRepository.findByPrediction_Id(predictionId);
        
        if (diseases.isEmpty()) {
            log.warn("Bu tahmin için hastalık bulunamadı - Prediction ID: {}", predictionId);
            throw new ResourceNotFoundException("Bu tahmin için hastalık bulunamadı - Prediction ID: " + predictionId);
        }
        
        return diseases;
    }
    
    @Override
    public PredictionDisease updatePredictionDisease(Integer id, Boolean isHealthy) {
        log.info("PredictionDisease güncelleniyor - ID: {}, Is Healthy: {}", id, isHealthy);
        
        PredictionDisease predictionDisease = predictionDiseaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PredictionDisease bulunamadı - ID: " + id));
        
        predictionDisease.setIsHealthy(isHealthy);
        
        PredictionDisease updated = predictionDiseaseRepository.save(predictionDisease);
        log.info("PredictionDisease güncellendi - ID: {}", id);
        
        return updated;
    }
    
    @Override
    public void deletePredictionDisease(Integer id) {
        log.info("PredictionDisease siliniyor - ID: {}", id);
        
        if (!predictionDiseaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("PredictionDisease bulunamadı - ID: " + id);
        }
        
        predictionDiseaseRepository.deleteById(id);
        log.info("PredictionDisease silindi - ID: {}", id);
    }
}
