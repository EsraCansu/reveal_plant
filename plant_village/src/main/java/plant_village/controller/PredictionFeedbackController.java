package plant_village.controller;

import plant_village.model.PredictionFeedback;
import plant_village.model.PredictionLog;
import plant_village.repository.PredictionFeedbackRepository;
import plant_village.repository.PredictionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Feedback Controller
 * Base URL: /api/feedbacks
 * 
 * Endpoints:
 * GET    /api/feedbacks                    - Get all feedbacks
 * GET    /api/feedbacks/{id}               - Get feedback by ID
 * GET    /api/feedbacks/prediction/{id}    - Get feedbacks for prediction
 * POST   /api/feedbacks                    - Create new feedback
 * PUT    /api/feedbacks/{id}               - Update feedback
 * DELETE /api/feedbacks/{id}               - Delete feedback
 */
@RestController
@RequestMapping("/api/feedbacks")
public class PredictionFeedbackController {

    @Autowired
    private PredictionFeedbackRepository feedbackRepository;

    /**
     * GET /api/feedbacks
     * Get all feedbacks
     */
    @GetMapping
    public ResponseEntity<List<PredictionFeedback>> getAllFeedbacks() {
        List<PredictionFeedback> feedbacks = feedbackRepository.findAll();
        return ResponseEntity.ok(feedbacks);
    }

    /**
     * GET /api/feedbacks/{id}
     * Get feedback by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getFeedbackById(@PathVariable Integer id) {
        Optional<PredictionFeedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            return ResponseEntity.ok(feedback.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/feedbacks/prediction/{predictionId}
     * Get all feedbacks for a prediction
     */
    @GetMapping("/prediction/{predictionId}")
    public ResponseEntity<List<PredictionFeedback>> getFeedbacksByPrediction(@PathVariable Integer predictionId) {
        List<PredictionFeedback> feedbacks = feedbackRepository.findByPrediction_Id(predictionId);
        return ResponseEntity.ok(feedbacks);
    }

    /**
     * POST /api/feedbacks
     * Create new feedback
     */
    @PostMapping
    public ResponseEntity<?> createFeedback(@RequestBody PredictionFeedback feedback) {
        try {
            PredictionFeedback savedFeedback = feedbackRepository.save(feedback);
            return ResponseEntity.status(201).body(savedFeedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating feedback: " + e.getMessage());
        }
    }

    /**
     * PUT /api/feedbacks/{id}
     * Update feedback
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFeedback(@PathVariable Integer id, @RequestBody PredictionFeedback feedbackDetails) {
        Optional<PredictionFeedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            PredictionFeedback existingFeedback = feedback.get();
            if (feedbackDetails.getIsApprovedFromAdmin() != null) existingFeedback.setIsApprovedFromAdmin(feedbackDetails.getIsApprovedFromAdmin());
            if (feedbackDetails.getComment() != null) existingFeedback.setComment(feedbackDetails.getComment());
            if (feedbackDetails.getImageAddedToDb() != null) existingFeedback.setImageAddedToDb(feedbackDetails.getImageAddedToDb());
            
            PredictionFeedback updatedFeedback = feedbackRepository.save(existingFeedback);
            return ResponseEntity.ok(updatedFeedback);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /api/feedbacks/{id}
     * Delete feedback
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Integer id) {
        Optional<PredictionFeedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            feedbackRepository.deleteById(id);
            return ResponseEntity.ok("Feedback deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * PUT /api/feedbacks/{id}/approve
     * Approve feedback by admin
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveFeedback(@PathVariable Integer id) {
        Optional<PredictionFeedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            PredictionFeedback existingFeedback = feedback.get();
            existingFeedback.setIsApprovedFromAdmin(true);
            PredictionFeedback updatedFeedback = feedbackRepository.save(existingFeedback);
            return ResponseEntity.ok(updatedFeedback);
        }
        return ResponseEntity.notFound().build();
    }
}

/**
 * Log Controller
 * Base URL: /api/logs
 * 
 * Endpoints:
 * GET    /api/logs                    - Get all logs
 * GET    /api/logs/{id}               - Get log by ID
 * GET    /api/logs/prediction/{id}    - Get logs for prediction
 * POST   /api/logs                    - Create new log
 * DELETE /api/logs/{id}               - Delete log
 */
@RestController
@RequestMapping("/api/logs")
class PredictionLogController {

    @Autowired
    private PredictionLogRepository logRepository;

    /**
     * GET /api/logs
     * Get all logs
     */
    @GetMapping
    public ResponseEntity<List<PredictionLog>> getAllLogs() {
        List<PredictionLog> logs = logRepository.findAll();
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/logs/{id}
     * Get log by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLogById(@PathVariable Integer id) {
        Optional<PredictionLog> log = logRepository.findById(id);
        if (log.isPresent()) {
            return ResponseEntity.ok(log.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/logs/prediction/{predictionId}
     * Get all logs for a prediction
     */
    @GetMapping("/prediction/{predictionId}")
    public ResponseEntity<List<PredictionLog>> getLogsByPrediction(@PathVariable Integer predictionId) {
        List<PredictionLog> logs = logRepository.findByPrediction_Id(predictionId);
        return ResponseEntity.ok(logs);
    }

    /**
     * POST /api/logs
     * Create new log
     */
    @PostMapping
    public ResponseEntity<?> createLog(@RequestBody PredictionLog log) {
        try {
            PredictionLog savedLog = logRepository.save(log);
            return ResponseEntity.status(201).body(savedLog);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating log: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/logs/{id}
     * Delete log
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLog(@PathVariable Integer id) {
        Optional<PredictionLog> log = logRepository.findById(id);
        if (log.isPresent()) {
            logRepository.deleteById(id);
            return ResponseEntity.ok("Log deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
