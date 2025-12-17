package plant_village.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastAPIPredictionRequest {
    @JsonProperty("image_base64")
    private String imageBase64;
    
    @JsonProperty("image_type")
    private String imageType;
    
    @JsonProperty("plant_id")
    private Integer plantId;
    
    private String description;
}
