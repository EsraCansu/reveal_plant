package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Integer> {

    // result demonstrating in detail
    // find disease features getter from modal
    Optional<Disease> findByName(String name); // Disease Entity's area name is 'name' 
    
    // Belirli bir bitki türüne ait olası tüm hastalıkları listelemek.
   // List<Disease> findByPlant_PlantId(Integer plantId);
}