package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionPlantRepository extends JpaRepository<PredictionPlant, PredictionPlantId> {
    
    // Result display - get plant relations for specific prediction
    List<PredictionPlant> findByPredictionId(Integer predictionId);
    
    // Get all predictions linked to a plant
    List<PredictionPlant> findByPlantId(Integer plantId);
    
    // Get specific prediction-plant link by composite key
    Optional<PredictionPlant> findByPredictionIdAndPlantId(Integer predictionId, Integer plantId);
}
