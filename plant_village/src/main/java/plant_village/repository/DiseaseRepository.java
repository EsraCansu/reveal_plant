package plant_village.repository;

import plant_village.model.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Integer> {

    // 1. Search by disease name
    Optional<Disease> findByDiseaseName(String diseaseName);

    // 2. Case-insensitive name search
    Optional<Disease> findByDiseaseNameIgnoreCase(String diseaseName);
    
    // 3. For feedback system - find disease by exact name match
    List<Disease> findByDiseaseNameContainingIgnoreCase(String keyword);
}