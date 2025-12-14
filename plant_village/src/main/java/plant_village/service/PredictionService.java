package plant_village.service;

import plant_village.model.Prediction;
import plant_village.model.User;
import java.util.List;
import java.util.Optional;

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
}