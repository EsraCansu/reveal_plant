package plant_village.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private Long predictionId;
    private Long userId;
    private Long plantId;
    private String plantName;
    private String diseaseName;
    private Double confidence;
    private String recommendedAction;
    private LocalDateTime predictedAt;
    private String status;  // SUCCESS, ERROR, PROCESSING
    private String message;
}
