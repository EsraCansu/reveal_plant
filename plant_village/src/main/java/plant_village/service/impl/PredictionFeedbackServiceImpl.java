package plant_village.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plant_village.exception.ResourceNotFoundException;
import plant_village.model.Prediction;
import plant_village.model.PredictionFeedback;
import plant_village.repository.PredictionFeedbackRepository;
import plant_village.repository.PredictionRepository;
import plant_village.service.PredictionFeedbackService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for Prediction Feedback operations.
 * Handles feedback submission, retrieval, and statistics calculation.
 * 
 * STEP 5: Feedback - User feedback on predictions
 */
@Service
@Slf4j
public class PredictionFeedbackServiceImpl implements PredictionFeedbackService {
    
    @Autowired
    private PredictionFeedbackRepository feedbackRepository;
    
    @Autowired
    private PredictionRepository predictionRepository;
    
    /**
     * Submit feedback for a prediction
     * Validates prediction exists and creates feedback entry
     * STEP 5: Feedback - Create feedback
     */
    @Override
    public PredictionFeedback submitFeedback(PredictionFeedback feedback) {
        log.info("Submitting feedback for prediction");
        
        // feedback object should contain prediction reference
        // Verify prediction exists
        if (feedback.getPrediction() == null || feedback.getPrediction().getId() == null) {
            log.warn("Prediction not found for feedback");
            throw new ResourceNotFoundException("Tahmin bulunamadı");
        }
        
        // Verify prediction exists in DB
        if (!predictionRepository.existsById(feedback.getPrediction().getId())) {
            log.warn("Prediction not found for feedback: {}", feedback.getPrediction().getId());
            throw new ResourceNotFoundException(
                "Tahmin bulunamadı - ID: " + feedback.getPrediction().getId()
            );
        }
        
        // Set creation timestamp if not provided
        if (feedback.getCreatedAt() == null) {
            feedback.setCreatedAt(LocalDateTime.now());
        }
        
        // Set default values
        if (feedback.getIsApprovedFromAdmin() == null) {
            feedback.setIsApprovedFromAdmin(false);
        }
        if (feedback.getImageAddedToDb() == null) {
            feedback.setImageAddedToDb(false);
        }
        
        PredictionFeedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback submitted successfully for prediction ID: {} (Feedback ID: {})",
            feedback.getPrediction().getId(), savedFeedback.getFeedbackId());
        
        // If this feedback is marked as correct, automatically save the image
        if (savedFeedback.getIsCorrect() != null && savedFeedback.getIsCorrect()) {
            log.info("📸 Feedback marked as correct - automatically saving image for prediction ID: {}", 
                feedback.getPrediction().getId());
            try {
                saveCorrectPredictionImage(savedFeedback);
            } catch (Exception e) {
                log.warn("⚠️ Failed to auto-save image for prediction ID: {} - {}", 
                    feedback.getPrediction().getId(), e.getMessage());
                // Don't fail the feedback submission if image saving fails
            }
        }
        
        return savedFeedback;
    }
    
    /**
     * Get all feedback ordered by creation date
     * STEP 5: Feedback - Retrieve all feedback (admin)
     */
    @Override
    public List<PredictionFeedback> getAllFeedback() {
        log.info("Fetching all feedback with user and prediction details");
        
        // Use custom query with JOIN FETCH to eagerly load user and prediction
        List<PredictionFeedback> feedbackList = feedbackRepository.findAllWithUserAndPrediction();
        
        log.info("Retrieved {} feedback entries with joined relations", feedbackList.size());
        
        return feedbackList;
    }
    
    /**
     * Get feedback for a specific prediction
     * STEP 5: Feedback - Get feedback for prediction
     */
    @Override
    public List<PredictionFeedback> getFeedbackByPredictionId(Integer predictionId) {
        log.info("Fetching feedback for prediction ID: {}", predictionId);
        
        // Verify prediction exists
        if (!predictionRepository.existsById(predictionId)) {
            log.warn("Prediction not found: {}", predictionId);
            throw new ResourceNotFoundException(
                "Tahmin bulunamadı - ID: " + predictionId
            );
        }
        
        List<PredictionFeedback> feedbackList = feedbackRepository.findAll().stream()
            .filter(f -> f.getPrediction() != null && f.getPrediction().getId().equals(predictionId))
            .toList();
        
        log.info("Retrieved {} feedback entries for prediction ID: {}", feedbackList.size(), predictionId);
        
        return feedbackList;
    }
    
    /**
     * Get feedback by user
     * STEP 5: Feedback - Get user's feedback history
     */
    @Override
    public List<PredictionFeedback> getFeedbackByUserId(Integer userId) {
        log.info("Fetching feedback for user ID: {}", userId);
        
        List<PredictionFeedback> feedbackList = feedbackRepository.findAll().stream()
            .filter(f -> f.getPrediction() != null && f.getPrediction().getUser() != null 
                && f.getPrediction().getUser().getId().equals(userId))
            .toList();
        
        log.info("Retrieved {} feedback entries for user ID: {}", feedbackList.size(), userId);
        
        return feedbackList;
    }
    
    /**
     * Get feedback statistics (total, correct, incorrect, accuracy)
     * STEP 5: Feedback - Get feedback statistics
     */
    @Override
    public Map<String, Object> getFeedbackStatistics() {
        log.info("Calculating feedback statistics");
        
        List<PredictionFeedback> allFeedback = feedbackRepository.findAll();
        
        if (allFeedback.isEmpty()) {
            log.info("No feedback found for statistics calculation");
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalFeedback", 0);
            emptyStats.put("correctPredictions", 0);
            emptyStats.put("incorrectPredictions", 0);
            emptyStats.put("accuracy", 0.0);
            emptyStats.put("approvedFeedback", 0);
            emptyStats.put("pendingFeedback", 0);
            return emptyStats;
        }
        
        long totalFeedback = allFeedback.size();
        long approvedFeedback = allFeedback.stream()
            .filter(f -> f.getIsApprovedFromAdmin() != null && f.getIsApprovedFromAdmin())
            .count();
        long pendingFeedback = allFeedback.stream()
            .filter(f -> f.getIsApprovedFromAdmin() == null || !f.getIsApprovedFromAdmin())
            .count();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalFeedback", totalFeedback);
        statistics.put("approvedFeedback", approvedFeedback);
        statistics.put("pendingFeedback", pendingFeedback);
        
        log.info("Feedback statistics calculated - Total: {}, Approved: {}, Pending: {}",
            totalFeedback, approvedFeedback, pendingFeedback);
        
        return statistics;
    }
    
    /**
     * Approve feedback by admin
     * STEP 5: Feedback - Admin approval
     * Mark image as added to DB for ML training when approved
     */
    @Override
    public PredictionFeedback approveFeedback(Integer feedbackId) {
        log.info("Approving feedback with ID: {}", feedbackId);
        
        return feedbackRepository.findById(feedbackId)
            .map(feedback -> {
                feedback.setIsApprovedFromAdmin(true);
                
                // When approved, mark image as added to DB for ML training
                feedback.setImageAddedToDb(true);
                log.info("✅ Feedback approved - Image marked for ML training. Feedback ID: {}", feedbackId);
                
                PredictionFeedback approved = feedbackRepository.save(feedback);
                log.info("Feedback approved successfully - ID: {}, imageAddedToDb: {}", 
                    feedbackId, feedback.getImageAddedToDb());
                return approved;
            })
            .orElseThrow(() -> {
                log.error("Feedback not found with ID: {}", feedbackId);
                return new RuntimeException("Feedback not found with ID: " + feedbackId);
            });
    }
    
    /**
     * Delete/Reject feedback by admin
     * STEP 5: Feedback - Admin rejection
     * @param feedbackId The feedback ID to delete
     * @return true if deleted successfully, false if not found
     */
    @Override
    public boolean deleteFeedback(Integer feedbackId) {
        log.info("Deleting/Rejecting feedback with ID: {}", feedbackId);
        
        if (feedbackRepository.existsById(feedbackId)) {
            feedbackRepository.deleteById(feedbackId);
            log.info("🗑️ Feedback deleted successfully - ID: {}", feedbackId);
            return true;
        } else {
            log.warn("Feedback not found with ID: {}", feedbackId);
            return false;
        }
    }
    
    /**
     * Process pending feedback (add images that weren't added yet)
     * STEP 5: Feedback - Process pending feedback
     */
    @Override
    public void processPendingFeedback() {
        log.info("Processing pending feedback");
        
        List<PredictionFeedback> pendingFeedback = feedbackRepository.findAll().stream()
            .filter(f -> f.getImageAddedToDb() != null && !f.getImageAddedToDb())
            .toList();
        
        if (pendingFeedback.isEmpty()) {
            log.info("No pending feedback to process");
            return;
        }
        
        // Process each pending feedback
        for (PredictionFeedback feedback : pendingFeedback) {
            try {
                // Only process feedback with comments/images
                if (feedback.getComment() != null) {
                    
                    // Mark as processed (image added to db)
                    feedback.setImageAddedToDb(true);
                    feedbackRepository.save(feedback);
                    
                    log.info("Processed feedback image - Feedback ID: {}", feedback.getFeedbackId());
                }
            } catch (Exception e) {
                log.error("Error processing feedback ID: {} - {}", feedback.getFeedbackId(), e.getMessage());
            }
        }
        
        log.info("Processed {} pending feedback entries", pendingFeedback.size());
    }

    /**
     * Approve and save images of predictions marked as correct
     * Retrieves all feedback with isCorrect=true and saves associated prediction images
     * Organizes images by plant type and disease in separate folders
     */
    @Override
    public Map<String, Object> approveCorrectImages() {
        log.info("🖼️ Starting approval process for correct prediction images");
        
        Map<String, Object> result = new HashMap<>();
        int totalProcessed = 0;
        int successCount = 0;
        int failureCount = 0;
        List<String> savedFiles = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();
        
        try {
            // Get absolute path for approve_img directory
            String basePath = System.getProperty("user.dir");
            String approveImgPath = basePath + File.separator + "approve_img";
            
            log.info("📁 Using base path: {}", approveImgPath);
            
            File approveBaseDir = new File(approveImgPath);
            if (!approveBaseDir.exists()) {
                approveBaseDir.mkdirs();
                log.info("✅ Created approve_img directory: {}", approveImgPath);
            }
            
            // Get all feedback with isCorrect = true
            List<PredictionFeedback> correctFeedbacks = feedbackRepository.findByIsCorrectTrue();
            log.info("📊 Found {} feedback entries marked as correct", correctFeedbacks.size());
            
            if (correctFeedbacks.isEmpty()) {
                log.warn("⚠️ No correct feedback found to process");
                result.put("status", "success");
                result.put("totalProcessed", 0);
                result.put("successCount", 0);
                result.put("failureCount", 0);
                result.put("savedFiles", savedFiles);
                result.put("errors", errors);
                result.put("message", "No correct feedback found to process");
                return result;
            }
            
            for (PredictionFeedback feedback : correctFeedbacks) {
                totalProcessed++;
                try {
                    // Get the associated prediction
                    Prediction prediction = feedback.getPrediction();
                    if (prediction == null) {
                        log.warn("⚠️ Prediction not found for feedback ID: {}", feedback.getFeedbackId());
                        failureCount++;
                        errors.add("Feedback ID " + feedback.getFeedbackId() + ": Prediction not found");
                        continue;
                    }
                    
                    String imageUrl = prediction.getUploadedImageUrl();
                    if (imageUrl == null || imageUrl.trim().isEmpty()) {
                        log.warn("⚠️ No image URL for prediction ID: {}", prediction.getId());
                        failureCount++;
                        errors.add("Prediction ID " + prediction.getId() + ": No image URL");
                        continue;
                    }
                    
                    // Create organized folder structure: 
                    // approve_img/plants/Bitki/prediction_X.png
                    // approve_img/diseases/Hastalık/prediction_X.png
                    
                    String plantType = prediction.getPredictionType();
                    if (plantType == null) {
                        plantType = "Unknown";
                    }
                    
                    // Parse format: "Plant___Disease" or "Plant - Disease"
                    String disease = "Unknown";
                    String plant = plantType;
                    
                    if (plantType.contains("___")) {
                        // Format: "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)"
                        String[] parts = plantType.split("___");
                        plant = parts[0].trim();
                        disease = (parts.length > 1) ? parts[1].trim() : "Unknown";
                    } else if (plantType.contains(" - ")) {
                        // Format: "Plant - Disease"
                        String[] parts = plantType.split(" - ");
                        plant = parts[0].trim();
                        disease = (parts.length > 1) ? parts[1].trim() : "Unknown";
                    } else if (plantType.toLowerCase().contains("healthy")) {
                        // Format: "Plant___healthy"
                        String[] parts = plantType.split("___");
                        plant = parts[0].trim();
                        disease = "healthy";
                    } else {
                        // Just plant name
                        plant = plantType.trim();
                    }
                    
                    // Clean up names for folder structure
                    plant = plant.replace(" ", "_").replace("(", "").replace(")", "").replace(",", "");
                    disease = disease.replace(" ", "_").replace("(", "").replace(")", "").replace(",", "");
                    
                    // Extract file extension from image URL or use default
                    String fileExtension = ".png";
                    if (imageUrl.contains(".")) {
                        String[] urlParts = imageUrl.split("\\.");
                        fileExtension = "." + urlParts[urlParts.length - 1].replaceAll("[?;].*", "");
                    }
                    
                    // Create filename
                    String fileName = "prediction_" + prediction.getId() + "_" + 
                                    System.currentTimeMillis() + fileExtension;
                    
                    // Create two separate organized paths
                    String plantPath = approveImgPath + File.separator + "plant" + File.separator + plant;
                    String diseasePath = approveImgPath + File.separator + "disease" + File.separator + disease;
                    
                    File plantDir = new File(plantPath);
                    File diseaseDir = new File(diseasePath);
                    
                    if (!plantDir.exists()) {
                        plantDir.mkdirs();
                        log.info("📁 Created plant folder: approve_img/plant/{}", plant);
                    }
                    if (!diseaseDir.exists()) {
                        diseaseDir.mkdirs();
                        log.info("📁 Created disease folder: approve_img/disease/{}", disease);
                    }
                    
                    String plantFilePath = plantPath + File.separator + fileName;
                    String diseaseFilePath = diseasePath + File.separator + fileName;
                    
                    log.info("💾 Processing image for prediction ID: {} - Plant: {}, Disease: {}", 
                            prediction.getId(), plant, disease);
                    
                    // Handle different image URL formats
                    if (imageUrl.startsWith("data:image")) {
                        // Base64 encoded image
                        String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
                        
                        // Save to plant folder
                        try (FileOutputStream fos = new FileOutputStream(plantFilePath)) {
                            fos.write(decodedBytes);
                            log.info("✅ Saved to plant folder: approve_img/plant/{}/{}", plant, fileName);
                        }
                        
                        // Save to disease folder
                        try (FileOutputStream fos = new FileOutputStream(diseaseFilePath)) {
                            fos.write(decodedBytes);
                            successCount++;
                            savedFiles.add(plantFilePath);
                            savedFiles.add(diseaseFilePath);
                            log.info("✅ Saved to disease folder: approve_img/disease/{}/{}", disease, fileName);
                        }
                    } else if (imageUrl.contains("uploads") || imageUrl.contains("\\") || imageUrl.startsWith("/")) {
                        // File system path (relative or absolute)
                        String sourcePath = imageUrl;
                        
                        // Try different path resolutions
                        File sourceFile = new File(sourcePath);
                        
                        // If relative path, try from current directory
                        if (!sourceFile.exists()) {
                            sourcePath = basePath + File.separator + imageUrl;
                            sourceFile = new File(sourcePath);
                        }
                        
                        // Try from uploads folder
                        if (!sourceFile.exists()) {
                            sourcePath = basePath + File.separator + "uploads" + File.separator + imageUrl.replace("uploads/", "").replace("uploads\\", "");
                            sourceFile = new File(sourcePath);
                        }
                        
                        if (sourceFile.exists() && sourceFile.isFile()) {
                            // Save to plant folder
                            Files.copy(sourceFile.toPath(), Paths.get(plantFilePath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            log.info("✅ Copied to plant folder: approve_img/plant/{}/{}", plant, fileName);
                            
                            // Save to disease folder
                            Files.copy(sourceFile.toPath(), Paths.get(diseaseFilePath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            successCount++;
                            savedFiles.add(plantFilePath);
                            savedFiles.add(diseaseFilePath);
                            log.info("✅ Copied to disease folder: approve_img/disease/{}/{}", disease, fileName);
                        } else {
                            log.warn("⚠️ Source file not found: {}", imageUrl);
                            failureCount++;
                            errors.add("Prediction ID " + prediction.getId() + ": Source file not found");
                        }
                    } else {
                        log.warn("⚠️ Cannot determine image URL format: {}", imageUrl);
                        failureCount++;
                        errors.add("Prediction ID " + prediction.getId() + ": Unknown image URL format");
                    }
                    
                } catch (IOException e) {
                    failureCount++;
                    String errorMsg = "Feedback ID " + feedback.getFeedbackId() + ": IOException - " + e.getMessage();
                    errors.add(errorMsg);
                    log.error("❌ Error saving image for feedback ID: {}", feedback.getFeedbackId(), e);
                } catch (Exception e) {
                    failureCount++;
                    String errorMsg = "Feedback ID " + feedback.getFeedbackId() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage();
                    errors.add(errorMsg);
                    log.error("❌ Unexpected error processing feedback ID: {}", feedback.getFeedbackId(), e);
                }
            }
            
            result.put("status", "success");
            result.put("totalProcessed", totalProcessed);
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("savedFiles", savedFiles);
            result.put("errors", errors);
            result.put("message", "✅ Image approval process completed: " + successCount + " saved, " + failureCount + " failed");
            
            log.info("📈 Approval process completed - Total: {}, Success: {}, Failed: {}", 
                    totalProcessed, successCount, failureCount);
            
        } catch (Exception e) {
            log.error("❌ Critical error in approveCorrectImages", e);
            result.put("status", "error");
            result.put("message", "Critical error: " + e.getMessage());
            result.put("error", e.toString());
        }
        
        return result;
    }

    /**
     * Helper method to save a single correct prediction image
     * Called when feedback is marked as correct
     */
    private void saveCorrectPredictionImage(PredictionFeedback feedback) {
        try {
            // Get absolute path for approve_img directory
            String basePath = System.getProperty("user.dir");
            String approveImgPath = basePath + File.separator + "approve_img";
            
            // Get the associated prediction
            Prediction prediction = feedback.getPrediction();
            if (prediction == null) {
                log.warn("⚠️ Prediction not found for feedback ID: {}", feedback.getFeedbackId());
                return;
            }
            
            String imageUrl = prediction.getUploadedImageUrl();
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                log.warn("⚠️ No image URL for prediction ID: {}", prediction.getId());
                return;
            }
            
            // Parse plant and disease from predictionType
            String plantType = prediction.getPredictionType();
            if (plantType == null) {
                plantType = "Unknown";
            }
            
            String disease = "Unknown";
            String plant = plantType;
            
            if (plantType.contains("___")) {
                String[] parts = plantType.split("___");
                plant = parts[0].trim();
                disease = (parts.length > 1) ? parts[1].trim() : "Unknown";
            } else if (plantType.contains(" - ")) {
                String[] parts = plantType.split(" - ");
                plant = parts[0].trim();
                disease = (parts.length > 1) ? parts[1].trim() : "Unknown";
            } else if (plantType.toLowerCase().contains("healthy")) {
                String[] parts = plantType.split("___");
                plant = parts[0].trim();
                disease = "healthy";
            } else {
                plant = plantType.trim();
            }
            
            plant = plant.replace(" ", "_").replace("(", "").replace(")", "").replace(",", "");
            disease = disease.replace(" ", "_").replace("(", "").replace(")", "").replace(",", "");
            
            // Create filename
            String fileExtension = ".png";
            if (imageUrl.contains(".")) {
                String[] urlParts = imageUrl.split("\\.");
                fileExtension = "." + urlParts[urlParts.length - 1].replaceAll("[?;].*", "");
            }
            String fileName = "prediction_" + prediction.getId() + "_" + System.currentTimeMillis() + fileExtension;
            
            // Create directory paths
            String plantPath = approveImgPath + File.separator + "plant" + File.separator + plant;
            String diseasePath = approveImgPath + File.separator + "disease" + File.separator + disease;
            
            File plantDir = new File(plantPath);
            File diseaseDir = new File(diseasePath);
            
            if (!plantDir.exists()) {
                plantDir.mkdirs();
            }
            if (!diseaseDir.exists()) {
                diseaseDir.mkdirs();
            }
            
            String plantFilePath = plantPath + File.separator + fileName;
            String diseaseFilePath = diseasePath + File.separator + fileName;
            
            // Save image based on URL format
            if (imageUrl.startsWith("data:image")) {
                // Base64 encoded
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
                
                try (FileOutputStream fos = new FileOutputStream(plantFilePath)) {
                    fos.write(decodedBytes);
                }
                try (FileOutputStream fos = new FileOutputStream(diseaseFilePath)) {
                    fos.write(decodedBytes);
                }
                log.info("✅ Auto-saved base64 image for prediction ID: {} ({}/{}) to approve_img", 
                    prediction.getId(), plant, disease);
            } else if (imageUrl.contains("uploads") || imageUrl.contains("\\") || imageUrl.startsWith("/")) {
                // File path
                String sourcePath = imageUrl;
                File sourceFile = new File(sourcePath);
                
                if (!sourceFile.exists()) {
                    sourcePath = basePath + File.separator + imageUrl;
                    sourceFile = new File(sourcePath);
                }
                
                if (!sourceFile.exists()) {
                    sourcePath = basePath + File.separator + "uploads" + File.separator + 
                        imageUrl.replace("uploads/", "").replace("uploads\\", "");
                    sourceFile = new File(sourcePath);
                }
                
                if (sourceFile.exists() && sourceFile.isFile()) {
                    Files.copy(sourceFile.toPath(), Paths.get(plantFilePath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(sourceFile.toPath(), Paths.get(diseaseFilePath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    log.info("✅ Auto-saved file image for prediction ID: {} ({}/{}) to approve_img", 
                        prediction.getId(), plant, disease);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error in saveCorrectPredictionImage: {}", e.getMessage(), e);
        }
    }
}
