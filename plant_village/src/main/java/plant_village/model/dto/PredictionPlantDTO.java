package plant_village.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionPlantDTO {
    private Integer predictionId;
    private Integer plantId;
    private String plantName;
    private Double confidence;
}
