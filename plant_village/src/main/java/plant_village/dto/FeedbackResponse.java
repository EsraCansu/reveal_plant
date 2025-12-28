package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * STEP 5: Add Feedback
 * Feedback Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Integer feedbackId;
    private Integer predictionId;
    private Boolean isCorrect;
    private Boolean isApprovedFromAdmin;
    private Boolean isApproved;
    private String comment;
    private LocalDateTime createdAt;
    private Boolean imageAddedToDb;
    
    private String message;
}
