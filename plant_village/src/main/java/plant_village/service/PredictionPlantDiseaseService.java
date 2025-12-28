package plant_village.service;

import plant_village.model.PredictionPlant;
import plant_village.model.PredictionDisease;
import java.util.List;
import java.util.Optional;

public interface PredictionPlantDiseaseService {
    
    // Plant linking
    PredictionPlant linkPlantToPrediction(Integer predictionId, Integer plantId, Float matchConfidence);
    
    Optional<PredictionPlant> getPredictionPlantById(Integer id);
    
    List<PredictionPlant> getPredictionPlantsByPredictionId(Integer predictionId);
    
    void deletePredictionPlant(Integer id);
    
    // Disease linking
    PredictionDisease linkDiseaseToPrediction(Integer predictionId, Integer diseaseId, Boolean isHealthy, Float matchConfidence);
    
    Optional<PredictionDisease> getPredictionDiseaseById(Integer id);
    
    List<PredictionDisease> getPredictionDiseasesByPredictionId(Integer predictionId);
    
    PredictionDisease updatePredictionDisease(Integer id, Boolean isHealthy);
    
    void deletePredictionDisease(Integer id);
}
