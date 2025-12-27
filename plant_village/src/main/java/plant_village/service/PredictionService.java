package plant_village.service;

import plant_village.model.*;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

public interface PredictionService {
    
    // create new proediction
    Prediction createPrediction(Prediction prediction);
    
    // get the user prediction history
    List<Prediction> getPredictionHistory(Integer userId);
    
    // get the invalid prediction for admin
    List<Prediction> getInvalidPredictionsForReview();
    
    // admin operation// update the predciton from prediction log
    Prediction updatePrediction(Integer predictionId, Prediction updatedPrediction, User adminUser);

    // get the prediction log with predictionId
    Optional<Prediction> findById(Integer predictionId);

    /**
     * Real-time prediction API
     * Processes plant disease prediction with image and sends result via WebSocket
     */
    Prediction predictPlantDisease(Integer userId, Integer plantId, String imageBase64, String description, String predictionMode);
}