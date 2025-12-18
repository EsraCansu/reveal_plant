package plant_village.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseSaveRequest {
    private Integer predictionId; // for connection main table
    private Integer diseaseId;    // represents in MS SQL
    private Boolean isHealthy;    // 1 or 0 status
}