package plant_village.service;

import plant_village.model.PredictionPlant;
import plant_village.model.PredictionDisease;
import java.util.List;
import java.util.Optional;

public interface PredictionPlantDiseaseService {
    
    // Plant linking
    PredictionPlant linkPlantToPrediction(Integer predictionId, Integer plantId, Float confidence);
    
    Optional<PredictionPlant> getPredictionPlantByCompositeKey(Integer predictionId, Integer plantId);
    
    List<PredictionPlant> getPredictionPlantsByPredictionId(Integer predictionId);
    
    void deletePredictionPlant(Integer predictionId, Integer plantId);
    
    // Disease linking
    PredictionDisease linkDiseaseToPrediction(Integer predictionId, Integer diseaseId, Boolean isHealthy, Float confidence);
    
    Optional<PredictionDisease> getPredictionDiseaseByCompositeKey(Integer predictionId, Integer diseaseId);
    
    List<PredictionDisease> getPredictionDiseasesByPredictionId(Integer predictionId);
    
    PredictionDisease updatePredictionDisease(Integer predictionId, Integer diseaseId, Boolean isHealthy);
    
    void deletePredictionDisease(Integer predictionId, Integer diseaseId);
}
