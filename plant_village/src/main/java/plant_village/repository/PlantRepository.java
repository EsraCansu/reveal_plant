package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Integer> {

    // find plant name from getter name
    Optional<Plant> findByPlantName(String plantName);
    
    // fast search from scientific name
    Optional<Plant> findByScientificName(String scientificName);
}
