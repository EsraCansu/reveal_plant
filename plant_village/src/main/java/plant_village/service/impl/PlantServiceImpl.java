package plant_village.service.impl;

import plant_village.model.Plant;
import plant_village.repository.PlantRepository;
import plant_village.service.PlantService;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PlantServiceImpl implements PlantService {
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Override
    public List<Plant> getAllPlants() {
        log.info("Listing all plants");
        return plantRepository.findAll();
    }
    
    @Override
    public Optional<Plant> getPlantById(Integer id) {
        log.info("Fetching plant - ID: {}", id);
        Optional<Plant> plant = plantRepository.findById(id);
        if (plant.isEmpty()) {
            log.warn("Plant not found - ID: {}", id);
            throw new ResourceNotFoundException("Plant not found - ID: " + id);
        }
        return plant;
    }
    
    @Override
    public List<Plant> searchByName(String name) {
        log.info("Searching plant - Name: {}", name);
        return plantRepository.findByPlantNameContainingIgnoreCase(name);
    }
    
    @Override
    public Plant createPlant(Plant plant) {
        log.info("Creating new plant - Name: {}", plant.getPlantName());
        return plantRepository.save(plant);
    }
    
    @Override
    public Plant updatePlant(Integer id, Plant plantDetails) {
        log.info("Updating plant - ID: {}", id);
        Plant plant = plantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plant not found - ID: " + id));
        
        if (plantDetails.getPlantName() != null) {
            plant.setPlantName(plantDetails.getPlantName());
        }
        if (plantDetails.getScientificName() != null) {
            plant.setScientificName(plantDetails.getScientificName());
        }
        if (plantDetails.getDescription() != null) {
            plant.setDescription(plantDetails.getDescription());
        }
        if (plantDetails.getCareTips() != null) {
            plant.setCareTips(plantDetails.getCareTips());
        }
        if (plantDetails.getImageUrl() != null) {
            plant.setImageUrl(plantDetails.getImageUrl());
        }
        if (plantDetails.getWateringFrequency() != null) {
            plant.setWateringFrequency(plantDetails.getWateringFrequency());
        }
        if (plantDetails.getSunlightRequirement() != null) {
            plant.setSunlightRequirement(plantDetails.getSunlightRequirement());
        }
        if (plantDetails.getSoilType() != null) {
            plant.setSoilType(plantDetails.getSoilType());
        }
        if (plantDetails.getHardinessZone() != null) {
            plant.setHardinessZone(plantDetails.getHardinessZone());
        }
        if (plantDetails.getValidClassification() != null) {
            plant.setValidClassification(plantDetails.getValidClassification());
        }
        
        return plantRepository.save(plant);
    }
    
    @Override
    public void deletePlant(Integer id) {
        log.info("Deleting plant - ID: {}", id);
        if (!plantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plant not found - ID: " + id);
        }
        plantRepository.deleteById(id);
        log.info("Plant deleted - ID: {}", id);
    }
}
