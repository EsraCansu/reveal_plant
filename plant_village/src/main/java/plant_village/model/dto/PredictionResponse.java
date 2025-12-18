package plant_village.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private Integer predictionId;
    private Integer userId;
    private Integer plantId;
    private String plantName;
    private String diseaseName;
    private Double confidence;
    private String recommendedAction;
    private LocalDateTime predictedAt;
    private String status;  // SUCCESS, ERROR, PROCESSING
    private String message;
}
