package plant_village.controller;

import plant_village.model.Disease;
import plant_village.repository.DiseaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Disease Controller
 * Base URL: /api/diseases
 * 
 * Endpoints:
 * GET    /api/diseases                    - Get all diseases
 * GET    /api/diseases/{id}               - Get disease by ID
 * GET    /api/diseases/search/{name}      - Search disease by name
 * POST   /api/diseases                    - Create new disease
 * PUT    /api/diseases/{id}               - Update disease
 * DELETE /api/diseases/{id}               - Delete disease
 */
@RestController
@RequestMapping("/api/diseases")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DiseaseController {

    @Autowired
    private DiseaseRepository diseaseRepository;

    /**
     * GET /api/diseases
     * Get all diseases
     */
    @GetMapping
    public ResponseEntity<List<Disease>> getAllDiseases() {
        List<Disease> diseases = diseaseRepository.findAll();
        return ResponseEntity.ok(diseases);
    }

    /**
     * GET /api/diseases/{id}
     * Get disease by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDiseaseById(@PathVariable Integer id) {
        Optional<Disease> disease = diseaseRepository.findById(id);
        if (disease.isPresent()) {
            return ResponseEntity.ok(disease.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/diseases/search/{name}
     * Search disease by name
     */
    @GetMapping("/search/{name}")
    public ResponseEntity<?> searchByName(@PathVariable String name) {
        Optional<Disease> disease = diseaseRepository.findByDiseaseNameIgnoreCase(name);
        if (disease.isPresent()) {
            return ResponseEntity.ok(disease.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/diseases/keyword/{keyword}
     * Search diseases by keyword
     */
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<List<Disease>> searchByKeyword(@PathVariable String keyword) {
        List<Disease> diseases = diseaseRepository.findByDiseaseNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(diseases);
    }

    /**
     * POST /api/diseases
     * Create new disease
     */
    @PostMapping
    public ResponseEntity<?> createDisease(@RequestBody Disease disease) {
        try {
            Disease savedDisease = diseaseRepository.save(disease);
            return ResponseEntity.status(201).body(savedDisease);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating disease: " + e.getMessage());
        }
    }

    /**
     * PUT /api/diseases/{id}
     * Update disease
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDisease(@PathVariable Integer id, @RequestBody Disease diseaseDetails) {
        Optional<Disease> disease = diseaseRepository.findById(id);
        if (disease.isPresent()) {
            Disease existingDisease = disease.get();
            if (diseaseDetails.getDiseaseName() != null) existingDisease.setDiseaseName(diseaseDetails.getDiseaseName());
            if (diseaseDetails.getSymptomDescription() != null) existingDisease.setSymptomDescription(diseaseDetails.getSymptomDescription());
            if (diseaseDetails.getCause() != null) existingDisease.setCause(diseaseDetails.getCause());
            if (diseaseDetails.getExampleImageUrl() != null) existingDisease.setExampleImageUrl(diseaseDetails.getExampleImageUrl());
            if (diseaseDetails.getTreatment() != null) existingDisease.setTreatment(diseaseDetails.getTreatment());
            
            Disease updatedDisease = diseaseRepository.save(existingDisease);
            return ResponseEntity.ok(updatedDisease);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /api/diseases/{id}
     * Delete disease
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDisease(@PathVariable Integer id) {
        Optional<Disease> disease = diseaseRepository.findById(id);
        if (disease.isPresent()) {
            diseaseRepository.deleteById(id);
            return ResponseEntity.ok("Disease deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
