package plant_village.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Disease")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disease {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disease_id")
    private Integer id;
    
    @Column(name = "disease_name", length = 100, nullable = false)
    private String diseaseName;
    
    @Column(name = "symptom_description", columnDefinition = "NVARCHAR(MAX)")
    private String symptomDescription;
    
    @Column(name = "cause", columnDefinition = "NVARCHAR(MAX)")
    private String cause;
    
    @Column(name = "treatment", columnDefinition = "NVARCHAR(MAX)")
    private String treatment;
    
    @Column(name = "recommended_medicines", columnDefinition = "NVARCHAR(MAX)")
    private String recommendedMedicines;
    
    @OneToMany(mappedBy = "disease", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<PredictionDisease> predictions;
}
