package plant_village.service;

import plant_village.model.*;
import plant_village.model.dto.FastAPIResponse;
import plant_village.repository.*;
import plant_village.util.PlantDiseaseCacheManager;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PredictionService Implementation
 * 
 * Implements the complete prediction workflow:
 * 1. FastAPI integration (Image analysis)
 * 2. %50 Rule validation (Confidence threshold)
 * 3. Tree structure storage (Top 3 predictions)
 * 4. Smart indicators (isHealthy detection)
 * 5. Stack management (PredictionLog for history)
 * 6. Hash map caching (O(1) lookups)
 */
@Slf4j
@Service
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final PredictionLogRepository predictionLogRepository; 
    private final PredictionDiseaseRepository predictionDiseaseRepository;
    private final PredictionPlantRepository predictionPlantRepository;
    private final FastAPIClientService fastAPIClientService;
    private final PlantDiseaseCacheManager cacheManager;
    private final UserRepository userRepository;

    // %50 Confidence Threshold - Rule: if confidence < 50%, mark as invalid
    private static final double CONFIDENCE_THRESHOLD = 0.50;

    @Autowired
    public PredictionServiceImpl(
        PredictionRepository predictionRepository, 
        PredictionLogRepository predictionLogRepository,
        PredictionDiseaseRepository predictionDiseaseRepository,
        PredictionPlantRepository predictionPlantRepository,
        FastAPIClientService fastAPIClientService,
        PlantDiseaseCacheManager cacheManager,
        UserRepository userRepository) {
        
        this.predictionRepository = predictionRepository;
        this.predictionLogRepository = predictionLogRepository;
        this.predictionDiseaseRepository = predictionDiseaseRepository;
        this.predictionPlantRepository = predictionPlantRepository;
        this.fastAPIClientService = fastAPIClientService;
        this.cacheManager = cacheManager;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Prediction createPrediction(Prediction prediction) {
        prediction.setCreatedAt(LocalDateTime.now());
        prediction.setIsValid(true);
        return predictionRepository.save(prediction);
    }

    /**
     * Main Prediction Workflow with complete algorithm implementation
     * 
     * Workflow Steps:
     * 1. Validate user exists
     * 2. Send image to FastAPI for analysis
     * 3. Validate confidence >= 50% (%50 Rule)
     * 4. Create root Prediction record
     * 5. Determine isHealthy indicator (contains "healthy"?)
     * 6. Store Top 3 results in Tree structure (PredictionPlant/PredictionDisease)
     * 7. Use Hash Map for O(1) lookups
     * 8. Create PredictionLog entry (Stack)
     * 
     * @param userId User ID
     * @param plantId Optional Plant ID (null for plant recognition)
     * @param imageBase64 Image in Base64 format
     * @param description Optional image description
     * @return Saved Prediction entity with complete information
     */
    @Override
    @Transactional
    public Prediction predictPlantDisease(
        Integer userId, 
        Integer plantId, 
        String imageBase64, 
        String description) {
        
        log.info("═══════════════════════════════════════════════════════════");
        log.info("🔄 STARTING PREDICTION WORKFLOW");
        log.info("═══════════════════════════════════════════════════════════");
        log.info("   👤 User ID: {}", userId);
        log.info("   🌿 Plant ID: {}", plantId != null ? plantId : "PLANT_RECOGNITION_MODE");
        log.info("═══════════════════════════════════════════════════════════");
        
        // Step 1: Get or create guest user
        User user = userRepository.findById(userId).orElseGet(() -> {
            log.info("⚠️  User not found, creating guest user...");
            User guestUser = User.builder()
                .id(userId)
                .userName("guest")
                .email("guest@revealplant.com")
                .passwordHash("$2a$10$dummyhashedpassword")
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();
            return userRepository.save(guestUser);
        });
        
        log.info("✅ [Step 1] User validated: {}", user.getUserName());

        // Step 2: Send to FastAPI for analysis
        Integer fastApiPlantId = plantId != null ? plantId : 0;
        log.info("📤 [Step 2] Sending image to FastAPI for analysis...");
        
        FastAPIResponse apiResponse = fastAPIClientService.predictDisease(
            fastApiPlantId, 
            imageBase64, 
            description
        );
        
        log.info("✅ [Step 2] FastAPI Analysis Complete:");
        log.info("   🎯 Top Prediction: {}", apiResponse.getTopPrediction());
        log.info("   📊 Confidence: {}%", String.format("%.2f", apiResponse.getTopConfidence() * 100));

        // Step 3: CONFIDENCE THRESHOLD VALIDATION (%50 Rule)
        log.info("🔍 [Step 3] Validating confidence threshold (50% rule)...");
        
        if (apiResponse.getTopConfidence() < CONFIDENCE_THRESHOLD) {
            log.warn("⚠️  CONFIDENCE BELOW THRESHOLD!");
            log.warn("    Confidence: {}% < Required: 50%", 
                    String.format("%.2f", apiResponse.getTopConfidence() * 100));
            log.info("    Decision: INVALID PREDICTION - Not Predictable");
            
            Prediction invalidPrediction = Prediction.builder()
                .user(user)
                .confidence(apiResponse.getTopConfidence())
                .createdAt(LocalDateTime.now())
                .predictionType(plantId == null ? "PLANT_PREDICTION" : "DISEASE_PREDICTION")
                .isValid(false)  // ❌ Invalid prediction
                .uploadedImageUrl("uploads/" + LocalDateTime.now().getNano() + ".jpg")
                .build();
            
            Prediction saved = predictionRepository.save(invalidPrediction);
            log.info("❌ Invalid prediction saved with ID: {}", saved.getId());
            return saved;
        }
        
        log.info("✅ [Step 3] Confidence threshold passed!");

        // Step 4: Create root prediction record
        log.info("🌳 [Step 4] Creating root Prediction record (Tree Root)...");
        
        // Store predictions list for later use
        List<plant_village.model.dto.DiseasePrediction> top3List = apiResponse.getPredictions();
        String top3Json = null;
        if (top3List != null && !top3List.isEmpty()) {
            // Convert to JSON string for storage (using US locale to avoid decimal separator issues)
            try {
                java.util.List<String> predStrings = new java.util.ArrayList<>();
                for (int i = 0; i < Math.min(3, top3List.size()); i++) {
                    plant_village.model.dto.DiseasePrediction p = top3List.get(i);
                    predStrings.add(String.format(java.util.Locale.US, "{\"disease\":\"%s\",\"confidence\":%.4f}", 
                        p.getDisease(), p.getConfidenceScore()));
                }
                top3Json = "[" + String.join(",", predStrings) + "]";
            } catch (Exception e) {
                log.warn("Failed to serialize predictions: {}", e.getMessage());
            }
        }
        
        Prediction prediction = Prediction.builder()
            .user(user)
            .confidence(apiResponse.getTopConfidence())
            .topPrediction(apiResponse.getTopPrediction())  // 🔥 Store ML result
            .description(top3Json)  // 🔥 Store Top 3 as JSON
            .createdAt(LocalDateTime.now())
            .predictionType(plantId == null ? "PLANT_PREDICTION" : "DISEASE_PREDICTION")
            .isValid(true)  // ✅ Valid prediction
            .uploadedImageUrl("uploads/" + LocalDateTime.now().getNano() + ".jpg")
            .build();

        Prediction savedPrediction = predictionRepository.save(prediction);
        log.info("✅ [Step 4] Root prediction saved with ID: {}", savedPrediction.getId());

        // Step 5: Determine isHealthy indicator
        log.info("🏥 [Step 5] Determining health status...");
        
        String topPredictionLabel = apiResponse.getTopPrediction().toLowerCase();
        boolean isHealthy = topPredictionLabel.contains("healthy");
        
        log.info("✅ [Step 5] Health status determined: isHealthy = {}", isHealthy);
        if (isHealthy) {
            log.info("           ✅ Plant is HEALTHY");
        } else {
            log.info("           ❌ Plant has DISEASE");
        }

        // Step 6: Store Top 3 results in Tree structure
        log.info("🌲 [Step 6] Storing Top 3 predictions in Tree structure...");
        
        List<plant_village.model.dto.DiseasePrediction> top3Predictions = apiResponse.getPredictions();
        int savedCount = 0;
        
        if (top3Predictions == null || top3Predictions.isEmpty()) {
            log.warn("⚠️ No predictions returned from FastAPI");
            top3Predictions = new java.util.ArrayList<>();
        }
        
        for (int i = 0; i < Math.min(3, top3Predictions.size()); i++) {
            plant_village.model.dto.DiseasePrediction topResult = top3Predictions.get(i);
            String label = topResult.getDisease();
            Double confidence = topResult.getConfidenceScore();
            
            log.info("     [{}/3] Label: '{}', Confidence: {}%", 
                    i+1, label, String.format("%.2f", confidence * 100));

            boolean resultIsHealthy = label.toLowerCase().contains("healthy");

            // Step 6A: If Plant prediction or result contains "healthy" → Store in PredictionPlant
            if (plantId == null || resultIsHealthy) {
                // Step 6A.1: Hash Map Lookup - O(1) operation
                Optional<Plant> plantOpt = cacheManager.getPlantByName(label);
                
                if (plantOpt.isPresent()) {
                    Plant plant = plantOpt.get();
                    PredictionPlant pp = PredictionPlant.builder()
                        .prediction(savedPrediction)
                        .plant(plant)
                        .confidence(confidence)
                        .build();
                    predictionPlantRepository.save(pp);
                    log.info("         ✅ Saved to PredictionPlant (Plant branch)");
                    savedCount++;
                } else {
                    log.warn("         ⚠️  Plant '{}' not found in cache", label);
                }
            } 
            // Step 6B: Otherwise → Store in PredictionDisease
            else {
                // Step 6B.1: Hash Map Lookup - O(1) operation
                Optional<Disease> diseaseOpt = cacheManager.getDiseaseByName(label);
                
                if (diseaseOpt.isPresent()) {
                    Disease disease = diseaseOpt.get();
                    PredictionDisease pd = PredictionDisease.builder()
                        .prediction(savedPrediction)
                        .disease(disease)
                        .isHealthy(false)  // Smart indicator
                        .build();
                    predictionDiseaseRepository.save(pd);
                    log.info("         ✅ Saved to PredictionDisease (Disease branch)");
                    savedCount++;
                } else {
                    log.warn("         ⚠️  Disease '{}' not found in cache", label);
                }
            }
        }
        
        log.info("✅ [Step 6] Tree structure complete: {} branches saved", savedCount);

        // Step 7: Create Prediction Log entry (Stack)
        log.info("📚 [Step 7] Adding to prediction history stack...");
        
        PredictionLog logEntry = PredictionLog.builder()
            .prediction(savedPrediction)
            .actionType("PREDICTION_CREATED")
            .timestamp(LocalDateTime.now())
            .newValue(String.format(
                "isHealthy=%s, confidence=%.2f%%, predictionType=%s",
                isHealthy,
                apiResponse.getTopConfidence() * 100,
                prediction.getPredictionType()
            ))
            .build();
        
        predictionLogRepository.save(logEntry);
        log.info("✅ [Step 7] Stack entry created (LIFO order maintained)");

        // Workflow complete
        log.info("═══════════════════════════════════════════════════════════");
        log.info("✅ PREDICTION WORKFLOW COMPLETED SUCCESSFULLY!");
        log.info("═══════════════════════════════════════════════════════════");
        log.info("   📋 Prediction ID: {}", savedPrediction.getId());
        log.info("   ✓ Valid: {}", savedPrediction.getIsValid());
        log.info("   ✓ Health: {}", isHealthy ? "HEALTHY" : "DISEASED");
        log.info("   ✓ Confidence: {}%", String.format("%.2f", savedPrediction.getConfidence() * 100));
        log.info("   ✓ Tree branches: {}", savedCount);
        log.info("═══════════════════════════════════════════════════════════\n");
        
        return savedPrediction;
    }

    /**
     * Get user's prediction history in LIFO order (Stack)
     * Most recent predictions appear first
     * 
     * @param userId User ID
     * @return List of predictions ordered by creation date (newest first)
     */
    @Override
    public List<Prediction> getPredictionHistory(Integer userId) {
        log.info("📖 Fetching prediction history for user: {} (LIFO order)", userId);
        return predictionRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get invalid predictions for review (failed %50 rule)
     */
    @Override
    public List<Prediction> getInvalidPredictionsForReview() {
        log.info("🔍 Fetching invalid predictions (confidence < 50%)");
        return predictionRepository.findByIsValid(false);
    }
    
    @Override
    public Optional<Prediction> findById(Integer predictionId) {
        return predictionRepository.findById(predictionId);
    }
    
    /**
     * Update prediction with logging
     */
    @Override
    @Transactional
    public Prediction updatePrediction(Integer predictionId, Prediction updatedPrediction, User adminUser) {
        Prediction existingPrediction = predictionRepository.findById(predictionId)
                                         .orElseThrow(() -> new ResourceNotFoundException("Tahmin kaydı bulunamadı."));
        
        String oldValue = "Confidence: " + existingPrediction.getConfidence() + ", Valid: " + existingPrediction.getIsValid(); 

        existingPrediction.setConfidence(updatedPrediction.getConfidence());
        existingPrediction.setIsValid(updatedPrediction.getIsValid());

        Prediction savedPrediction = predictionRepository.save(existingPrediction);

        PredictionLog logEntry = PredictionLog.builder()
                .prediction(savedPrediction)
                .adminUser(adminUser)
                .actionType("UPDATE")
                .timestamp(LocalDateTime.now())
                .oldValue(oldValue)
                .newValue("Confidence: " + savedPrediction.getConfidence() + ", Valid: " + savedPrediction.getIsValid())
                .build();

        predictionLogRepository.save(logEntry);

        return savedPrediction;
    }
}