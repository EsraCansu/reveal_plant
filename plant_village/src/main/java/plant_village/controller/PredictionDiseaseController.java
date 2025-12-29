package plant_village.controller;

import plant_village.model.PredictionDisease;
import plant_village.model.PredictionDiseaseId;
import plant_village.model.dto.PredictionDiseaseDTO;
import plant_village.repository.PredictionDiseaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PredictionDisease Controller
 * Base URL: /api/prediction-diseases
 * 
 * Handles relationship between Predictions and Diseases (with composite key)
 * 
 * Endpoints:
 * GET    /api/prediction-diseases                            - Get all prediction-disease relationships
 * GET    /api/prediction-diseases/{predictionId}/{diseaseId} - Get by composite key
 * GET    /api/prediction-diseases/prediction/{id}            - Get diseases for a prediction
 * GET    /api/prediction-diseases/disease/{id}               - Get predictions for a disease
 * POST   /api/prediction-diseases                            - Create new relationship
 * PUT    /api/prediction-diseases/{predictionId}/{diseaseId} - Update relationship
 * DELETE /api/prediction-diseases/{predictionId}/{diseaseId} - Delete relationship
 */
@RestController
@RequestMapping("/api/prediction-diseases")
public class PredictionDiseaseController {

    @Autowired
    private PredictionDiseaseRepository predictionDiseaseRepository;

    /**
     * GET /api/prediction-diseases
     * Get all prediction-disease relationships
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<PredictionDiseaseDTO>> getAllPredictionDiseases() {
        List<PredictionDisease> relationships = predictionDiseaseRepository.findAll();
        List<PredictionDiseaseDTO> dtoList = relationships.stream()
            .map(pd -> PredictionDiseaseDTO.builder()
                .predictionId(pd.getPredictionId())
                .diseaseId(pd.getDiseaseId())
                .diseaseName(pd.getDisease() != null ? pd.getDisease().getDiseaseName() : null)
                .isHealthy(pd.getIsHealthy())
                .confidence(pd.getConfidence())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * GET /api/prediction-diseases/{predictionId}/{diseaseId}
     * Get by composite key (predictionId, diseaseId)
     */
    @GetMapping("/{predictionId}/{diseaseId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPredictionDiseaseByCompositeKey(
            @PathVariable Integer predictionId,
            @PathVariable Integer diseaseId) {
        Optional<PredictionDisease> relationship = predictionDiseaseRepository.findByPredictionIdAndDiseaseId(predictionId, diseaseId);
        if (relationship.isPresent()) {
            PredictionDisease pd = relationship.get();
            PredictionDiseaseDTO dto = PredictionDiseaseDTO.builder()
                .predictionId(pd.getPredictionId())
                .diseaseId(pd.getDiseaseId())
                .diseaseName(pd.getDisease() != null ? pd.getDisease().getDiseaseName() : null)
                .isHealthy(pd.getIsHealthy())
                .confidence(pd.getConfidence())
                .build();
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/prediction-diseases/prediction/{predictionId}
     * Get all diseases linked to a prediction
     */
    @GetMapping("/prediction/{predictionId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PredictionDiseaseDTO>> getDiseasesByPrediction(@PathVariable Integer predictionId) {
        List<PredictionDisease> relationships = predictionDiseaseRepository.findByPredictionId(predictionId);
        List<PredictionDiseaseDTO> dtoList = relationships.stream()
            .map(pd -> PredictionDiseaseDTO.builder()
                .predictionId(pd.getPredictionId())
                .diseaseId(pd.getDiseaseId())
                .diseaseName(pd.getDisease() != null ? pd.getDisease().getDiseaseName() : null)
                .isHealthy(pd.getIsHealthy())
                .confidence(pd.getConfidence())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * GET /api/prediction-diseases/disease/{diseaseId}
     * Get all predictions linked to a disease
     */
    @GetMapping("/disease/{diseaseId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PredictionDiseaseDTO>> getPredictionsByDisease(@PathVariable Integer diseaseId) {
        List<PredictionDisease> relationships = predictionDiseaseRepository.findByDiseaseId(diseaseId);
        List<PredictionDiseaseDTO> dtoList = relationships.stream()
            .map(pd -> PredictionDiseaseDTO.builder()
                .predictionId(pd.getPredictionId())
                .diseaseId(pd.getDiseaseId())
                .diseaseName(pd.getDisease() != null ? pd.getDisease().getDiseaseName() : null)
                .isHealthy(pd.getIsHealthy())
                .confidence(pd.getConfidence())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * POST /api/prediction-diseases
     * Create new prediction-disease relationship
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> createPredictionDisease(@RequestBody PredictionDisease relationship) {
        try {
            PredictionDisease saved = predictionDiseaseRepository.save(relationship);
            PredictionDiseaseDTO dto = PredictionDiseaseDTO.builder()
                .predictionId(saved.getPredictionId())
                .diseaseId(saved.getDiseaseId())
                .diseaseName(saved.getDisease() != null ? saved.getDisease().getDiseaseName() : null)
                .isHealthy(saved.getIsHealthy())
                .confidence(saved.getConfidence())
                .build();
            return ResponseEntity.status(201).body(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating relationship: " + e.getMessage());
        }
    }

    /**
     * PUT /api/prediction-diseases/{predictionId}/{diseaseId}
     * Update prediction-disease relationship
     */
    @PutMapping("/{predictionId}/{diseaseId}")
    @Transactional
    public ResponseEntity<?> updatePredictionDisease(
            @PathVariable Integer predictionId,
            @PathVariable Integer diseaseId,
            @RequestBody PredictionDisease details) {
        Optional<PredictionDisease> relationship = predictionDiseaseRepository.findByPredictionIdAndDiseaseId(predictionId, diseaseId);
        if (relationship.isPresent()) {
            PredictionDisease existing = relationship.get();
            if (details.getIsHealthy() != null) {
                existing.setIsHealthy(details.getIsHealthy());
            }
            PredictionDisease updated = predictionDiseaseRepository.save(existing);
            PredictionDiseaseDTO dto = PredictionDiseaseDTO.builder()
                .predictionId(updated.getPredictionId())
                .diseaseId(updated.getDiseaseId())
                .diseaseName(updated.getDisease() != null ? updated.getDisease().getDiseaseName() : null)
                .isHealthy(updated.getIsHealthy())
                .confidence(updated.getConfidence())
                .build();
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /api/prediction-diseases/{predictionId}/{diseaseId}
     * Delete prediction-disease relationship using composite key
     */
    @DeleteMapping("/{predictionId}/{diseaseId}")
    @Transactional
    public ResponseEntity<?> deletePredictionDisease(
            @PathVariable Integer predictionId,
            @PathVariable Integer diseaseId) {
        PredictionDiseaseId id = PredictionDiseaseId.builder()
            .predictionId(predictionId)
            .diseaseId(diseaseId)
            .build();
        
        if (predictionDiseaseRepository.existsById(id)) {
            predictionDiseaseRepository.deleteById(id);
            return ResponseEntity.ok("Relationship deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
