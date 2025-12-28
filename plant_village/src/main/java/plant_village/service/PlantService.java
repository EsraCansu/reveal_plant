package plant_village.service;

import plant_village.model.Plant;
import java.util.List;
import java.util.Optional;

public interface PlantService {
    
    List<Plant> getAllPlants();
    
    Optional<Plant> getPlantById(Integer id);
    
    List<Plant> searchByName(String name);
    
    Plant createPlant(Plant plant);
    
    Plant updatePlant(Integer id, Plant plant);
    
    void deletePlant(Integer id);
}
