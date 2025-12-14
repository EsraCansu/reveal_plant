package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionDiseaseRepository extends JpaRepository<PredictionDisease, Integer> {
    
    // display prediction result
    // get the spesific disease results
    List<PredictionDisease> findByPrediction_PredictionId(Integer predictionId);
}