package plant_village.controller;

import plant_village.model.PredictionPlant;
import plant_village.model.PredictionDisease;
import plant_village.repository.PredictionPlantRepository;
import plant_village.repository.PredictionDiseaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * PredictionPlant Controller
 * Base URL: /api/prediction-plants
 * 
 * Handles relationship between Predictions and Plants
 * 
 * Endpoints:
 * GET    /api/prediction-plants                    - Get all prediction-plant relationships
 * GET    /api/prediction-plants/{id}               - Get by ID
 * GET    /api/prediction-plants/prediction/{id}    - Get plants for a prediction
 * GET    /api/prediction-plants/plant/{id}         - Get predictions for a plant
 * POST   /api/prediction-plants                    - Create new relationship
 * DELETE /api/prediction-plants/{id}               - Delete relationship
 */
@RestController
@RequestMapping("/api/prediction-plants")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PredictionPlantController {

    @Autowired
    private PredictionPlantRepository predictionPlantRepository;

    /**
     * GET /api/prediction-plants
     * Get all prediction-plant relationships
     */
    @GetMapping
    public ResponseEntity<List<PredictionPlant>> getAllPredictionPlants() {
        List<PredictionPlant> relationships = predictionPlantRepository.findAll();
        return ResponseEntity.ok(relationships);
    }

    /**
     * GET /api/prediction-plants/{id}
     * Get by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPredictionPlantById(@PathVariable Integer id) {
        Optional<PredictionPlant> relationship = predictionPlantRepository.findById(id);
        if (relationship.isPresent()) {
            return ResponseEntity.ok(relationship.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/prediction-plants/prediction/{predictionId}
     * Get all plants linked to a prediction
     */
    @GetMapping("/prediction/{predictionId}")
    public ResponseEntity<List<PredictionPlant>> getPlantsByPrediction(@PathVariable Integer predictionId) {
        List<PredictionPlant> relationships = predictionPlantRepository.findByPrediction_Id(predictionId);
        return ResponseEntity.ok(relationships);
    }

    /**
     * GET /api/prediction-plants/plant/{plantId}
     * Get all predictions linked to a plant
     */
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<List<PredictionPlant>> getPredictionsByPlant(@PathVariable Integer plantId) {
        List<PredictionPlant> relationships = predictionPlantRepository.findByPlant_Id(plantId);
        return ResponseEntity.ok(relationships);
    }

    /**
     * POST /api/prediction-plants
     * Create new prediction-plant relationship
     */
    @PostMapping
    public ResponseEntity<?> createPredictionPlant(@RequestBody PredictionPlant relationship) {
        try {
            PredictionPlant saved = predictionPlantRepository.save(relationship);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating relationship: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/prediction-plants/{id}
     * Delete prediction-plant relationship
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePredictionPlant(@PathVariable Integer id) {
        Optional<PredictionPlant> relationship = predictionPlantRepository.findById(id);
        if (relationship.isPresent()) {
            predictionPlantRepository.deleteById(id);
            return ResponseEntity.ok("Relationship deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}

/**
 * PredictionDisease Controller
 * Base URL: /api/prediction-diseases
 * 
 * Handles relationship between Predictions and Diseases
 * 
 * Endpoints:
 * GET    /api/prediction-diseases                    - Get all prediction-disease relationships
 * GET    /api/prediction-diseases/{id}               - Get by ID
 * GET    /api/prediction-diseases/prediction/{id}    - Get diseases for a prediction
 * GET    /api/prediction-diseases/disease/{id}       - Get predictions for a disease
 * POST   /api/prediction-diseases                    - Create new relationship
 * PUT    /api/prediction-diseases/{id}               - Update relationship
 * DELETE /api/prediction-diseases/{id}               - Delete relationship
 */
@RestController
@RequestMapping("/api/prediction-diseases")
@CrossOrigin(origins = "*", maxAge = 3600)
class PredictionDiseaseController {

    @Autowired
    private PredictionDiseaseRepository predictionDiseaseRepository;

    /**
     * GET /api/prediction-diseases
     * Get all prediction-disease relationships
     */
    @GetMapping
    public ResponseEntity<List<PredictionDisease>> getAllPredictionDiseases() {
        List<PredictionDisease> relationships = predictionDiseaseRepository.findAll();
        return ResponseEntity.ok(relationships);
    }

    /**
     * GET /api/prediction-diseases/{id}
     * Get by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPredictionDiseaseById(@PathVariable Integer id) {
        Optional<PredictionDisease> relationship = predictionDiseaseRepository.findById(id);
        if (relationship.isPresent()) {
            return ResponseEntity.ok(relationship.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/prediction-diseases/prediction/{predictionId}
     * Get all diseases linked to a prediction
     */
    @GetMapping("/prediction/{predictionId}")
    public ResponseEntity<List<PredictionDisease>> getDiseasesByPrediction(@PathVariable Integer predictionId) {
        List<PredictionDisease> relationships = predictionDiseaseRepository.findByPrediction_Id(predictionId);
        return ResponseEntity.ok(relationships);
    }

    /**
     * GET /api/prediction-diseases/disease/{diseaseId}
     * Get all predictions linked to a disease
     */
    @GetMapping("/disease/{diseaseId}")
    public ResponseEntity<List<PredictionDisease>> getPredictionsByDisease(@PathVariable Integer diseaseId) {
        List<PredictionDisease> relationships = predictionDiseaseRepository.findByDisease_Id(diseaseId);
        return ResponseEntity.ok(relationships);
    }

    /**
     * POST /api/prediction-diseases
     * Create new prediction-disease relationship
     */
    @PostMapping
    public ResponseEntity<?> createPredictionDisease(@RequestBody PredictionDisease relationship) {
        try {
            PredictionDisease saved = predictionDiseaseRepository.save(relationship);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating relationship: " + e.getMessage());
        }
    }

    /**
     * PUT /api/prediction-diseases/{id}
     * Update prediction-disease relationship
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePredictionDisease(@PathVariable Integer id, @RequestBody PredictionDisease details) {
        Optional<PredictionDisease> relationship = predictionDiseaseRepository.findById(id);
        if (relationship.isPresent()) {
            PredictionDisease existing = relationship.get();
            if (details.getIsHealthy() != null) {
                existing.setIsHealthy(details.getIsHealthy());
            }
            PredictionDisease updated = predictionDiseaseRepository.save(existing);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /api/prediction-diseases/{id}
     * Delete prediction-disease relationship
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePredictionDisease(@PathVariable Integer id) {
        Optional<PredictionDisease> relationship = predictionDiseaseRepository.findById(id);
        if (relationship.isPresent()) {
            predictionDiseaseRepository.deleteById(id);
            return ResponseEntity.ok("Relationship deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
