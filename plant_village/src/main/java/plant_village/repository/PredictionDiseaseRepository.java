package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionDiseaseRepository extends JpaRepository<PredictionDisease, PredictionDiseaseId> {
    
    // display prediction results - get disease results for specific prediction
    List<PredictionDisease> findByPredictionId(Integer predictionId);
    
    // Get all predictions linked to a disease
    List<PredictionDisease> findByDiseaseId(Integer diseaseId);
    
    // Get specific prediction-disease link by composite key
    Optional<PredictionDisease> findByPredictionIdAndDiseaseId(Integer predictionId, Integer diseaseId);
}