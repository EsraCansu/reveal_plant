package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionPlantRepository extends JpaRepository<PredictionPlant, Integer> {
    
    // Result display
    // getting plant relations some spesific plant
    List<PredictionPlant> findByPrediction_PredictionId(Integer predictionId);
}

