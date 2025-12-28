package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionDiseaseRepository extends JpaRepository<PredictionDisease, Integer> {
    
    // display prediction results - get disease results for specific prediction
    List<PredictionDisease> findByPrediction_Id(Integer predictionId);
    
    // Get all predictions linked to a disease
    List<PredictionDisease> findByDisease_Id(Integer diseaseId);
}