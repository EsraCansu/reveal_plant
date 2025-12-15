package plant_village.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {
    private Integer userId;
    private Integer plantId;
    private String imageBase64;
    private String description;
    private LocalDateTime requestedAt;
}
