package plant_village.service;

import plant_village.model.Disease;
import java.util.List;
import java.util.Optional;

public interface DiseaseService {
    
    List<Disease> getAllDiseases();
    
    Optional<Disease> getDiseaseById(Integer id);
    
    List<Disease> searchByName(String name);
    
    List<Disease> searchByKeyword(String keyword);
    
    Disease createDisease(Disease disease);
    
    Disease updateDisease(Integer id, Disease disease);
    
    void deleteDisease(Integer id);
}
