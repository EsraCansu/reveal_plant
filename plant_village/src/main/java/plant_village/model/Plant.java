package plant_village.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Plant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_id")
    private Integer id;
    
    // Getter alias for ID consistency
    public Integer getPlantId() {
        return this.id;
    }
    
    // Setter alias for ID consistency
    public void setPlantId(Integer plantId) {
        this.id = plantId;
    }
    
    @Column(name = "plant_name", length = 50)
    private String plantName;
    
    @Column(name = "scientific_name", length = 50)
    private String scientificName;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "valid_classification", nullable = false)
    private Boolean validClassification;
}
