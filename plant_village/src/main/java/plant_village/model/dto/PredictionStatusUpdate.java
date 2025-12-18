package plant_village.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionStatusUpdate {
    private Integer predictionId;
    private String status;  // UPLOADED, PROCESSING, ANALYZING, COMPLETE, ERROR
    private Integer progressPercentage;
    private String message;
    private LocalDateTime updatedAt;
}
