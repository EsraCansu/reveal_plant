package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * STEP 6: Create Audit Log
 * Log Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogResponse {

    private Integer logId;
    private Integer predictionId;
    private String actionType;
    private LocalDateTime timestamp;
    
    private String message;
}
