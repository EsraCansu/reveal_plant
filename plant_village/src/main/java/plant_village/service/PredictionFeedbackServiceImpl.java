package plant_village.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plant_village.model.*;
import plant_village.repository.*;

import java.util.*;

/**
 * Service implementation for PredictionFeedback operations.
 * Handles feedback submission and automatic image addition to plant/disease tables.
 */
@Service
public class PredictionFeedbackServiceImpl implements PredictionFeedbackService {
    
    @Autowired
    private PredictionFeedbackRepository feedbackRepository;
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Autowired
    private DiseaseRepository diseaseRepository;
    
    @Autowired
    private PlantUserImagesRepository plantUserImagesRepository;
    
    @Autowired
    private DiseaseUserImagesRepository diseaseUserImagesRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Submit feedback and automatically add image to database if positive
     */
    @Override
    @Transactional
    public PredictionFeedback submitFeedback(PredictionFeedback feedback) {
        // Save the feedback first
        PredictionFeedback savedFeedback = feedbackRepository.save(feedback);
        
        // If feedback is positive (correct), add image to respective user images table
        if (feedback.getIsCorrect()) {
            addImageToDatabase(savedFeedback);
        }
        
        return savedFeedback;
    }
    
    /**
     * Add validated image to plant or disease user images table
     */
    private void addImageToDatabase(PredictionFeedback feedback) {
        try {
            if ("PLANT".equalsIgnoreCase(feedback.getPredictionType())) {
                addPlantImage(feedback);
            } 
            else if ("DISEASE".equalsIgnoreCase(feedback.getPredictionType())) {
                addDiseaseImage(feedback);
            }
        } catch (Exception e) {
            // Log error but don't fail the feedback save
            System.err.println("Error adding image to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Add plant image to user images table
     */
    private void addPlantImage(PredictionFeedback feedback) {
        // Find plant by name
        Optional<Plant> plantOpt = plantRepository.findByPlantName(feedback.getPredictedName());
        
        if (plantOpt.isPresent()) {
            Plant plant = plantOpt.get();
            
            // Check if image already exists to avoid duplicates
            if (!plantUserImagesRepository.existsByPlantIdAndImageUrl(
                    plant.getPlantId(), feedback.getImageUrl())) {
                
                PlantUserImages userImage = PlantUserImages.builder()
                    .plantId(plant.getPlantId())
                    .plantName(plant.getPlantName())
                    .imageUrl(feedback.getImageUrl())
                    .submittedByUser(feedback.getUser())
                    .feedback(feedback)
                    .verified(true)
                    .build();
                
                plantUserImagesRepository.save(userImage);
                
                // Mark feedback as added to DB
                feedback.setImageAddedToDb(true);
                feedbackRepository.save(feedback);
                
                System.out.println("Added plant image to user images: " + plant.getPlantName());
            }
        }
    }
    
    /**
     * Add disease image to user images table
     */
    private void addDiseaseImage(PredictionFeedback feedback) {
        // Find disease by name
        Optional<Disease> diseaseOpt = diseaseRepository.findByDiseaseName(feedback.getPredictedName());
        
        if (diseaseOpt.isPresent()) {
            Disease disease = diseaseOpt.get();
            
            // Check if image already exists to avoid duplicates
            if (!diseaseUserImagesRepository.existsByDiseaseIdAndImageUrl(
                    disease.getDiseaseId(), feedback.getImageUrl())) {
                
                DiseaseUserImages userImage = DiseaseUserImages.builder()
                    .diseaseId(disease.getDiseaseId())
                    .diseaseName(disease.getDiseaseName())
                    .imageUrl(feedback.getImageUrl())
                    .submittedByUser(feedback.getUser())
                    .feedback(feedback)
                    .verified(true)
                    .build();
                
                diseaseUserImagesRepository.save(userImage);
                
                // Mark feedback as added to DB
                feedback.setImageAddedToDb(true);
                feedbackRepository.save(feedback);
                
                System.out.println("Added disease image to user images: " + disease.getDiseaseName());
            }
        }
    }
    
    @Override
    public List<PredictionFeedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Override
    public List<PredictionFeedback> getFeedbackByPredictionId(Integer predictionId) {
        return feedbackRepository.findByPredictionId(predictionId);
    }
    
    @Override
    public List<PredictionFeedback> getFeedbackByUserId(Integer userId) {
        return feedbackRepository.findByUser_Id(userId);
    }
    
    @Override
    public Map<String, Object> getFeedbackStatistics() {
        long total = feedbackRepository.count();
        long correct = feedbackRepository.countByIsCorrectTrue();
        long incorrect = feedbackRepository.countByIsCorrectFalse();
        double accuracy = total > 0 ? (correct * 100.0 / total) : 0;
        
        // Get plant and disease specific stats
        long plantTotal = feedbackRepository.countByPredictionType("PLANT");
        long diseaseTotal = feedbackRepository.countByPredictionType("DISEASE");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("correct", correct);
        stats.put("incorrect", incorrect);
        stats.put("accuracy", String.format("%.1f", accuracy));
        stats.put("plantFeedback", plantTotal);
        stats.put("diseaseFeedback", diseaseTotal);
        
        return stats;
    }
    
    @Override
    @Transactional
    public void processPendingFeedback() {
        List<PredictionFeedback> pendingFeedback = 
            feedbackRepository.findByIsCorrectTrueAndImageAddedToDbFalse();
        
        for (PredictionFeedback feedback : pendingFeedback) {
            addImageToDatabase(feedback);
        }
        
        System.out.println("Processed " + pendingFeedback.size() + " pending feedback items");
    }
}
