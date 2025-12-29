package plant_village.controller;

import plant_village.model.PredictionPlant;
import plant_village.model.PredictionPlantId;
import plant_village.model.dto.PredictionPlantDTO;
import plant_village.repository.PredictionPlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PredictionPlant Controller
 * Base URL: /api/prediction-plants
 * 
 * Handles relationship between Predictions and Plants (with composite key)
 * 
 * Endpoints:
 * GET    /api/prediction-plants                           - Get all prediction-plant relationships
 * GET    /api/prediction-plants/{predictionId}/{plantId}  - Get by composite key
 * GET    /api/prediction-plants/prediction/{id}           - Get plants for a prediction
 * GET    /api/prediction-plants/plant/{id}                - Get predictions for a plant
 * POST   /api/prediction-plants                           - Create new relationship
 * DELETE /api/prediction-plants/{predictionId}/{plantId}  - Delete relationship
 */
@RestController
@RequestMapping("/api/prediction-plants")
public class PredictionPlantController {

    @Autowired
    private PredictionPlantRepository predictionPlantRepository;

    /**
     * GET /api/prediction-plants
     * Get all prediction-plant relationships
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<PredictionPlantDTO>> getAllPredictionPlants() {
        List<PredictionPlant> relationships = predictionPlantRepository.findAll();
        List<PredictionPlantDTO> dtoList = relationships.stream()
            .map(pp -> PredictionPlantDTO.builder()
                .predictionId(pp.getPredictionId())
                .plantId(pp.getPlantId())
                .plantName(pp.getPlant() != null ? pp.getPlant().getPlantName() : null)
                .confidence(pp.getConfidence())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * GET /api/prediction-plants/{predictionId}/{plantId}
     * Get by composite key (predictionId, plantId)
     */
    @GetMapping("/{predictionId}/{plantId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPredictionPlantByCompositeKey(
            @PathVariable Integer predictionId,
            @PathVariable Integer plantId) {
        Optional<PredictionPlant> relationship = predictionPlantRepository.findByPredictionIdAndPlantId(predictionId, plantId);
        if (relationship.isPresent()) {
            PredictionPlant pp = relationship.get();
            PredictionPlantDTO dto = PredictionPlantDTO.builder()
                .predictionId(pp.getPredictionId())
                .plantId(pp.getPlantId())
                .plantName(pp.getPlant() != null ? pp.getPlant().getPlantName() : null)
                .confidence(pp.getConfidence())
                .build();
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/prediction-plants/prediction/{predictionId}
     * Get all plants linked to a prediction
     */
    @GetMapping("/prediction/{predictionId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PredictionPlantDTO>> getPlantsByPrediction(@PathVariable Integer predictionId) {
        List<PredictionPlant> relationships = predictionPlantRepository.findByPredictionId(predictionId);
        List<PredictionPlantDTO> dtoList = relationships.stream()
            .map(pp -> PredictionPlantDTO.builder()
                .predictionId(pp.getPredictionId())
                .plantId(pp.getPlantId())
                .plantName(pp.getPlant() != null ? pp.getPlant().getPlantName() : null)
                .confidence(pp.getConfidence())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * GET /api/prediction-plants/plant/{plantId}
     * Get all predictions linked to a plant
     */
    @GetMapping("/plant/{plantId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PredictionPlantDTO>> getPredictionsByPlant(@PathVariable Integer plantId) {
        List<PredictionPlant> relationships = predictionPlantRepository.findByPlantId(plantId);
        List<PredictionPlantDTO> dtoList = relationships.stream()
            .map(pp -> PredictionPlantDTO.builder()
                .predictionId(pp.getPredictionId())
                .plantId(pp.getPlantId())
                .plantName(pp.getPlant() != null ? pp.getPlant().getPlantName() : null)
                .confidence(pp.getConfidence())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * POST /api/prediction-plants
     * Create new prediction-plant relationship
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> createPredictionPlant(@RequestBody PredictionPlant relationship) {
        try {
            PredictionPlant saved = predictionPlantRepository.save(relationship);
            PredictionPlantDTO dto = PredictionPlantDTO.builder()
                .predictionId(saved.getPredictionId())
                .plantId(saved.getPlantId())
                .plantName(saved.getPlant() != null ? saved.getPlant().getPlantName() : null)
                .confidence(saved.getConfidence())
                .build();
            return ResponseEntity.status(201).body(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating relationship: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/prediction-plants/{predictionId}/{plantId}
     * Delete prediction-plant relationship using composite key
     */
    @DeleteMapping("/{predictionId}/{plantId}")
    @Transactional
    public ResponseEntity<?> deletePredictionPlant(
            @PathVariable Integer predictionId,
            @PathVariable Integer plantId) {
        PredictionPlantId id = PredictionPlantId.builder()
            .predictionId(predictionId)
            .plantId(plantId)
            .build();
        
        if (predictionPlantRepository.existsById(id)) {
            predictionPlantRepository.deleteById(id);
            return ResponseEntity.ok("Relationship deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
