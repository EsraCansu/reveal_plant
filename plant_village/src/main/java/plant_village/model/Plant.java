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
    
    @Column(name = "plant_name", length = 100, nullable = false)
    private String plantName;
    
    @Column(name = "scientific_name", length = 100)
    private String scientificName;
    
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;
    
    @Column(name = "care_tips", columnDefinition = "NVARCHAR(MAX)")
    private String careTips;
    
    @Column(name = "watering_frequency", length = 50)
    private String wateringFrequency;
    
    @Column(name = "sunlight_requirement", length = 100)
    private String sunlightRequirement;
    
    @Column(name = "soil_type", length = 100)
    private String soilType;
    
    @Column(name = "hardiness_zone", length = 50)
    private String hardinessZone;
    
    @Column(name = "valid_classification")
    private Boolean validClassification;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<PredictionPlant> predictions;
}
