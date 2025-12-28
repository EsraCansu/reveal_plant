package plant_village.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plant_village.exception.ResourceNotFoundException;
import plant_village.model.Prediction;
import plant_village.model.PredictionPlant;
import plant_village.model.PredictionDisease;
import plant_village.model.Disease;
import plant_village.repository.PredictionRepository;
import plant_village.repository.UserRepository;
import plant_village.repository.DiseaseRepository;
import plant_village.repository.PlantRepository;
import plant_village.service.PredictionService;
import plant_village.service.FastAPIClientService;
import plant_village.model.dto.FastAPIResponse;
import plant_village.model.dto.DiseasePrediction;
import plant_village.model.dto.PredictionResult;
import plant_village.model.PredictionLog;
import plant_village.model.Plant;
import plant_village.repository.PredictionLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Service implementation for Prediction operations.
 * Handles prediction creation, history retrieval, and disease prediction logic.
 * 
 * STEP 3: Main Prediction - Create prediction
 * STEP 7: Retrieve - Get prediction details
 */
@Service
@Slf4j
public class PredictionServiceImpl implements PredictionService {
    
    @Autowired
    private PredictionRepository predictionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DiseaseRepository diseaseRepository;
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Autowired
    private FastAPIClientService fastAPIClientService;
    
    @Autowired
    private PredictionLogRepository predictionLogRepository;
    
    @Autowired(required = false)
    private plant_village.util.PlantDiseaseCacheManager cacheManager;
    
    /**
     * Get cache manager for external use
     * Used by controller for plant/disease lookup
     */
    public plant_village.util.PlantDiseaseCacheManager getCacheManager() {
        return cacheManager;
    }
    
    /**
     * Create new prediction
     * STEP 3: Main Prediction - Create prediction with image
     */
    @Override
    public Prediction createPrediction(Prediction prediction) {
        log.info("Creating prediction for user ID: {}", prediction.getUser().getId());
        
        // Verify user exists
        if (prediction.getUser() == null || prediction.getUser().getId() == null) {
            log.warn("User not found for prediction");
            throw new ResourceNotFoundException("Kullanıcı bulunamadı");
        }
        
        // Set default values
        if (prediction.getCreateAt() == null) {
            prediction.setCreateAt(LocalDateTime.now());
        }
        if (prediction.getIsValid() == null) {
            prediction.setIsValid(false);  // Initially false until verified
        }
        
        Prediction savedPrediction = predictionRepository.save(prediction);
        log.info("Prediction created successfully - User ID: {}, Prediction ID: {}", 
            prediction.getUser().getId(), savedPrediction.getId());
        
        return savedPrediction;
    }
    
    /**
     * Get user's prediction history
     * STEP 7: Retrieve - Get user's prediction history
     */
    @Override
    public List<Prediction> getPredictionHistory(Integer userId) {
        log.info("Fetching prediction history for user ID: {}", userId);
        
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            log.warn("User not found: {}", userId);
            throw new ResourceNotFoundException(
                "Kullanıcı bulunamadı - ID: " + userId
            );
        }
        
        // Get all predictions for this user
        List<Prediction> predictions = predictionRepository.findAll().stream()
            .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
            .toList();
        
        // Sort by creation date descending (newest first)
        List<Prediction> sorted = new java.util.ArrayList<>(predictions);
        sorted.sort((p1, p2) -> p2.getCreateAt().compareTo(p1.getCreateAt()));
        
        log.info("Retrieved {} predictions for user ID: {}", sorted.size(), userId);
        
        return sorted;
    }
    
    /**
     * Get invalid predictions for admin review
     * Returns predictions marked as invalid
     * STEP 7: Retrieve - Admin get invalid predictions
     */
    @Override
    public List<Prediction> getInvalidPredictionsForReview() {
        log.info("Fetching invalid predictions for review");
        
        List<Prediction> allPredictions = predictionRepository.findAll();
        
        List<Prediction> invalidPredictions = allPredictions.stream()
            .filter(p -> p.getIsValid() != null && !p.getIsValid())
            .toList();
        
        log.info("Retrieved {} invalid predictions for review", invalidPredictions.size());
        
        return invalidPredictions;
    }
    
    /**
     * Update prediction (admin operation)
     * Admin can update prediction status
     * STEP 3: Main Prediction - Update prediction status
     */
    @Override
    public Prediction updatePrediction(Integer predictionId, Prediction updatedPrediction, 
                                       plant_village.model.User adminUser) {
        log.info("Updating prediction ID: {} by admin: {}", predictionId, adminUser.getUserName());
        
        // Verify prediction exists
        Prediction existingPrediction = predictionRepository.findById(predictionId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Tahmin bulunamadı - ID: " + predictionId
            ));
        
        // Update allowed fields
        if (updatedPrediction.getIsValid() != null) {
            existingPrediction.setIsValid(updatedPrediction.getIsValid());
        }
        if (updatedPrediction.getConfidence() != null) {
            existingPrediction.setConfidence(updatedPrediction.getConfidence());
        }
        if (updatedPrediction.getCareTips() != null) {
            existingPrediction.setCareTips(updatedPrediction.getCareTips());
        }
        
        Prediction saved = predictionRepository.save(existingPrediction);
        log.info("Prediction updated successfully - ID: {}, Is Valid: {}", 
            predictionId, saved.getIsValid());
        
        return saved;
    }
    
    /**
     * Find prediction by ID
     * STEP 7: Retrieve - Get prediction details
     */
    @Override
    public Optional<Prediction> findById(Integer predictionId) {
        log.info("Fetching prediction by ID: {}", predictionId);
        return predictionRepository.findById(predictionId);
    }
    
    /**
     * Real-time prediction API
     * Processes plant disease prediction with image
     * STEP 3: Main Prediction - Create prediction with ML model
     * @return PredictionResult containing saved prediction and all ML predictions
     */
    @Override
    public PredictionResult predictPlantDisease(Integer userId, Integer plantId, 
                                          String imageBase64, String description, String predictionMode) {
        log.info("Processing plant disease prediction for user ID: {}, plant ID: {}", userId, plantId);
        
        try {
            // Get user
            plant_village.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı - ID: " + userId));
            
            // Call FastAPI ML service
            log.info("🔄 Calling FastAPI ML service for prediction...");
            FastAPIResponse mlResponse = fastAPIClientService.predictDisease(plantId, imageBase64, description);
            
            if (mlResponse == null || "error".equals(mlResponse.getStatus())) {
                String errorMsg = mlResponse != null ? mlResponse.getMessage() : "No response from ML service";
                log.error("❌ FastAPI returned error: {}", errorMsg);
                throw new RuntimeException("ML tahmin hatası: " + errorMsg);
            }
            
            log.info("✅ FastAPI prediction received: {} with confidence {}", 
                mlResponse.getTopPrediction(), mlResponse.getTopConfidence());
            
            // Process based on prediction mode
            boolean isPlantMode = "identify-plant".equals(predictionMode);
            log.info("🎯 Prediction mode: {} (isPlantMode={})", predictionMode, isPlantMode);
            
            // Determine prediction type based on mode
            String predictionTypeValue;
            if (isPlantMode) {
                // For plant identification: extract just the plant name (e.g., "Grape")
                predictionTypeValue = extractPlantName(mlResponse.getTopPrediction());
            } else {
                // For disease detection: use normalized disease name (e.g., "Grape___Leaf_blight")
                predictionTypeValue = normalizeDiseaseName(mlResponse.getTopPrediction());
            }
            
            // Create new prediction record
            Prediction prediction = new Prediction();
            prediction.setUser(user);
            prediction.setUploadedImageUrl(imageBase64);  // Store base64 image
            prediction.setCareTips(description);
            prediction.setPredictionType(predictionTypeValue);  // Plant name or Disease name based on mode
            prediction.setConfidence(mlResponse.getTopConfidence());      // Overall confidence
            
            // Set validity based on 50% threshold
            boolean isValid = mlResponse.getTopConfidence() != null && mlResponse.getTopConfidence() >= 0.5;
            prediction.setIsValid(isValid);
            prediction.setCreateAt(LocalDateTime.now());
            
            if (isPlantMode) {
                // PLANT IDENTIFICATION MODE - Save to prediction_plant table (TOP 1 only)
                List<PredictionPlant> plantDetails = new ArrayList<>();
                
                if (mlResponse.getPredictions() != null && !mlResponse.getPredictions().isEmpty()) {
                    // Only add the TOP 1 prediction with highest confidence
                    DiseasePrediction topPrediction = mlResponse.getPredictions().get(0);
                    
                    String fullName = topPrediction.getDisease();
                    String plantName = extractPlantName(fullName);
                    
                    Optional<Plant> plantOpt = findPlantByName(plantName);
                    
                    if (plantOpt.isPresent()) {
                        Plant plant = plantOpt.get();
                        
                        PredictionPlant pp = new PredictionPlant();
                        pp.setPrediction(prediction);
                        pp.setPlant(plant);
                        pp.setMatchConfidence(topPrediction.getConfidenceScore());
                        
                        plantDetails.add(pp);
                        log.info("🌱 Added plant match: {} (id={}) with confidence {}", plant.getPlantName(), plant.getId(), topPrediction.getConfidenceScore());
                    } else {
                        log.warn("⚠️ Plant not found in DB: {}", plantName);
                    }
                }
                
                prediction.setPlantDetails(plantDetails);
                
            } else {
                // DISEASE DETECTION MODE - Save to prediction_disease table
                List<PredictionDisease> diseaseDetails = new ArrayList<>();
                
                if (mlResponse.getPredictions() != null && !mlResponse.getPredictions().isEmpty()) {
                    // Only add the TOP 1 prediction with highest confidence
                    DiseasePrediction topPrediction = mlResponse.getPredictions().get(0);
                    
                    String diseaseName = topPrediction.getDisease();
                    Optional<Disease> diseaseOpt = findDiseaseByNormalizedName(diseaseName);
                    
                    if (diseaseOpt.isPresent()) {
                        Disease disease = diseaseOpt.get();
                        
                        PredictionDisease pd = new PredictionDisease();
                        pd.setPrediction(prediction);
                        pd.setDisease(disease);
                        pd.setMatchConfidence(topPrediction.getConfidenceScore());
                        
                        diseaseDetails.add(pd);
                        log.info("🦠 Added disease match: {} (id={}) with confidence {}", disease.getDiseaseName(), disease.getId(), topPrediction.getConfidenceScore());
                    } else {
                        log.warn("⚠️ Disease not found in DB: {}", diseaseName);
                    }
                }
                
                prediction.setDiseaseDetails(diseaseDetails);
            }
            
            // Save prediction with relationships
            Prediction savedPrediction = predictionRepository.save(prediction);
            
            // Create PredictionLog entry
            PredictionLog logEntry = PredictionLog.builder()
                .prediction(savedPrediction)
                .actionType("PREDICTION_CREATED")
                .timestamp(LocalDateTime.now())
                .build();
            predictionLogRepository.save(logEntry);
            
            log.info("✅ Plant disease prediction processed - Prediction ID: {}, Type: {}, Confidence: {}", 
                savedPrediction.getId(), savedPrediction.getPredictionType(), savedPrediction.getConfidence());
            
            // Return PredictionResult with saved prediction AND all ML predictions for "Other Possibilities"
            return PredictionResult.builder()
                .prediction(savedPrediction)
                .allPredictions(mlResponse.getPredictions())
                .build();
        } catch (Exception e) {
            log.error("Error processing plant disease prediction: {}", e.getMessage(), e);
            throw new RuntimeException("Tahmin işleme hatası: " + e.getMessage());
        }
    }
    
    /**
     * Normalize ML disease name to DB format
     * Handles all PlantVillage ML model format differences:
     * - Corn_(maize)___... → Corn___...
     * - Cherry_(including_sour)___... → Cherry___...
     * - Pepper,_bell___... → Pepper__bell___...
     */
    private String normalizeDiseaseName(String mlName) {
        if (mlName == null) return "";
        
        String normalized = mlName;
        
        // 1. Handle Pepper,_bell → Pepper__bell (comma to double underscore)
        normalized = normalized.replace("Pepper,_bell", "Pepper__bell");
        
        // 2. Remove parenthetical suffixes like (maize), (including_sour)
        // Cherry_(including_sour)___... → Cherry___...
        // Corn_(maize)___... → Corn___...
        normalized = normalized.replaceAll("_?\\([^)]+\\)", "");
        
        return normalized.trim();
    }
    
    /**
     * Find disease by normalized name
     * Handles format differences between ML model and database
     * e.g., "Corn_(maize)___Cercospora_leaf_spot" from ML vs "Corn___Cercospora_leaf_spot" in DB
     */
    private Optional<Disease> findDiseaseByNormalizedName(String mlDiseaseName) {
        if (mlDiseaseName == null || mlDiseaseName.isEmpty()) {
            return Optional.empty();
        }
        
        // Normalize ML name to DB format (remove parenthetical like (maize))
        String normalizedName = normalizeDiseaseName(mlDiseaseName);
        log.debug("Searching disease: ML='{}' → Normalized='{}'", mlDiseaseName, normalizedName);
        
        // Try exact match first with normalized name
        Optional<Disease> exact = diseaseRepository.findByDiseaseNameIgnoreCase(normalizedName);
        if (exact.isPresent()) {
            log.debug("Found disease with normalized name: {}", normalizedName);
            return exact;
        }
        
        // Try original ML name
        exact = diseaseRepository.findByDiseaseNameIgnoreCase(mlDiseaseName);
        if (exact.isPresent()) {
            return exact;
        }
        
        // Try with ___ → __ conversion
        String convertedName = normalizedName.replace("___", "__");
        Optional<Disease> normalized = diseaseRepository.findByDiseaseNameIgnoreCase(convertedName);
        if (normalized.isPresent()) {
            log.debug("Found disease with ___ → __ conversion: {}", convertedName);
            return normalized;
        }
        
        // Try partial match as last resort
        List<Disease> partialMatches = diseaseRepository.findByDiseaseNameContainingIgnoreCase(
            normalizedName.replace("___", "_").replace("__", "_")
        );
        if (!partialMatches.isEmpty()) {
            log.debug("Found disease with partial match for: {}", mlDiseaseName);
            return Optional.of(partialMatches.get(0));
        }
        
        return Optional.empty();
    }
    
    /**
     * Extract plant name from ML prediction format
     * e.g., "Tomato___Leaf_Mold" → "Tomato"
     * e.g., "Grape___healthy" → "Grape"
     * e.g., "Corn_(maize)___..." → "Corn"
     */
    private String extractPlantName(String fullPrediction) {
        if (fullPrediction == null || fullPrediction.isEmpty()) {
            return "";
        }
        
        // Split by ___ (ML format) or __ (DB format)
        String[] parts = fullPrediction.split("___");
        if (parts.length > 0) {
            String plantName = parts[0];
            // Normalize ML model plant names to DB format
            plantName = normalizePlantName(plantName);
            return plantName;
        }
        
        parts = fullPrediction.split("__");
        if (parts.length > 0) {
            String plantName = parts[0];
            plantName = normalizePlantName(plantName);
            return plantName;
        }
        
        return normalizePlantName(fullPrediction);
    }
    
    /**
     * Normalize ML model plant names to DB format
     * Handles all PlantVillage ML model format differences:
     * - Corn_(maize) → Corn
     * - Cherry_(including_sour) → Cherry  
     * - Pepper,_bell → Pepper
     */
    private String normalizePlantName(String name) {
        if (name == null) return "";
        
        String normalized = name;
        
        // 1. Remove parenthetical suffixes like (maize), (including_sour)
        // Corn_(maize) → Corn
        // Cherry_(including_sour) → Cherry
        normalized = normalized.replaceAll("_?\\([^)]+\\)", "");
        
        // 2. Handle comma cases: Pepper,_bell → Pepper
        if (normalized.contains(",")) {
            normalized = normalized.split(",")[0];
        }
        
        // 3. Handle underscore prefix if any: _bell → remove leading underscore
        normalized = normalized.replaceAll("^_+", "");
        
        return normalized.trim();
    }
    
    /**
     * Find plant by name with normalization
     */
    private Optional<Plant> findPlantByName(String plantName) {
        if (plantName == null || plantName.isEmpty()) {
            return Optional.empty();
        }
        
        // Try exact match first
        Optional<Plant> exact = plantRepository.findByPlantName(plantName);
        if (exact.isPresent()) {
            return exact;
        }
        
        // Try case-insensitive search
        List<Plant> matches = plantRepository.findByPlantNameContainingIgnoreCase(plantName);
        if (!matches.isEmpty()) {
            // Return exact case-insensitive match if exists
            for (Plant p : matches) {
                if (p.getPlantName().equalsIgnoreCase(plantName)) {
                    return Optional.of(p);
                }
            }
            // Otherwise return first partial match
            return Optional.of(matches.get(0));
        }
        
        return Optional.empty();
    }
}
