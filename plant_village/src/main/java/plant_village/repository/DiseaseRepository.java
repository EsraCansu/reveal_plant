package plant_village.repository;

import plant_village.model.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Integer> {

    // 1. Bitki ID'sine göre hastalıkları bul (Doğru)
    List<Disease> findByPlant_Id(Integer plantId);
    
    // 2. İsim ve Bitki ID'sine göre (GÜNCELLENDİ: DiseaseName -> Name)
    List<Disease> findByNameAndPlant_Id(String name, Integer plantId);

    // 3. Büyük/Küçük harf duyarsız isimle arama (GÜNCELLENDİ: DiseaseName -> Name)
    // Service katmanında çağırdığımız metot bu!
    Optional<Disease> findByNameIgnoreCase(String name);
}