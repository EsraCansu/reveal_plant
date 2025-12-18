package plant_village.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantSaveRequest {
    private Integer predictionId;
    private Integer plantId;
}