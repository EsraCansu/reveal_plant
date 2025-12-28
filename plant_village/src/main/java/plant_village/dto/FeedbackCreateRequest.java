package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * STEP 5: Add Feedback
 * Feedback Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackCreateRequest {

    @NotNull(message = "Prediction ID boş olamaz")
    private Integer predictionId;

    @NotNull(message = "Is Correct boş olamaz")
    private Boolean isCorrect;

    private String comment;

    private Boolean isApproved;

    @Builder.Default
    private Boolean imageAddedToDb = false;
}
