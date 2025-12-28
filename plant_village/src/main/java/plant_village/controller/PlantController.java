package plant_village.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import plant_village.dto.ApiResponse;
import plant_village.dto.PlantResponse;
import plant_village.model.Plant;
import plant_village.service.PlantService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Plant Controller
 * Handles /api/plants/* endpoints for plant dictionary operations
 * 
 * STEP 2A: Dictionary - Get plant list and search
 */
@RestController
@RequestMapping("/api/plants")
@Validated
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlantController {

    @Autowired
    private PlantService plantService;

    /**
     * GET /api/plants
     * Get all plants
     * STEP 2A: Dictionary - Get plant list
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlantResponse>>> getAllPlants() {
        try {
            log.info("Fetching all plants");
            
            List<Plant> plants = plantService.getAllPlants();
            List<PlantResponse> responses = plants.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            log.info("Retrieved {} plants", responses.size());
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("Error fetching all plants: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    /**
     * GET /api/plants/{id}
     * Get plant by ID
     * STEP 2A: Dictionary - Get plant details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantResponse>> getPlantById(@PathVariable Integer id) {
        try {
            log.info("Fetching plant by ID: {}", id);
            
            Plant plant = plantService.getPlantById(id)
                .orElseThrow(() -> new RuntimeException("Plant not found - ID: " + id));
            PlantResponse response = convertToResponse(plant);
            
            log.info("Plant retrieved successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error fetching plant ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }
    }

    /**
     * GET /api/plants/search/{name}
     * Search plant by name
     * STEP 2A: Dictionary - Search plants
     */
    @GetMapping("/search/{name}")
    public ResponseEntity<ApiResponse<List<PlantResponse>>> searchByName(@PathVariable String name) {
        try {
            log.info("Searching plants by name: {}", name);
            
            List<Plant> plants = plantService.searchByName(name);
            List<PlantResponse> responses = plants.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            log.info("Found {} plants matching name: {}", responses.size(), name);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("Error searching plants by name {}: {}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    /**
     * POST /api/plants
     * Create new plant (admin)
     * STEP 2A: Dictionary - Create plant
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PlantResponse>> createPlant(@RequestBody Plant plant) {
        try {
            log.info("Creating new plant: {}", plant.getPlantName());
            
            Plant createdPlant = plantService.createPlant(plant);
            PlantResponse response = convertToResponse(createdPlant);
            
            log.info("Plant created successfully: {} (ID: {})", createdPlant.getPlantName(), createdPlant.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Plant created successfully"));
        } catch (Exception e) {
            log.error("Error creating plant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    /**
     * PUT /api/plants/{id}
     * Update plant (admin)
     * STEP 2A: Dictionary - Update plant
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantResponse>> updatePlant(
            @PathVariable Integer id,
            @RequestBody Plant plant) {
        try {
            log.info("Updating plant: {}", id);
            
            Plant updatedPlant = plantService.updatePlant(id, plant);
            PlantResponse response = convertToResponse(updatedPlant);
            
            log.info("Plant updated successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(response, "Plant updated successfully"));
        } catch (Exception e) {
            log.error("Error updating plant {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }
    }

    /**
     * DELETE /api/plants/{id}
     * Delete plant (admin)
     * STEP 2A: Dictionary - Delete plant
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePlant(@PathVariable Integer id) {
        try {
            log.info("Deleting plant: {}", id);
            
            plantService.deletePlant(id);
            
            log.info("Plant deleted successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Plant deleted successfully", "Delete operation completed"));
        } catch (Exception e) {
            log.error("Error deleting plant {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }
    }

    /**
     * Helper method to convert Plant entity to PlantResponse DTO
     */
    private PlantResponse convertToResponse(Plant plant) {
        return PlantResponse.builder()
            .plantId(plant.getId())
            .plantName(plant.getPlantName())
            .scientificName(plant.getScientificName())
            .description(plant.getDescription())
            .careTips(plant.getCareTips())
            .wateringFrequency(plant.getWateringFrequency())
            .sunlightRequirement(plant.getSunlightRequirement())
            .soilType(plant.getSoilType())
            .hardinessZone(plant.getHardinessZone())
            .build();
    }
}
