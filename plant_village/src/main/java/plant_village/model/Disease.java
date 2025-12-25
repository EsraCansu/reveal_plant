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
    
    // Getter alias for ID consistency
    public Integer getDiseaseId() {
        return this.id;
    }
    
    // Setter alias for ID consistency
    public void setDiseaseId(Integer diseaseId) {
        this.id = diseaseId;
    }
    
    @Column(name = "disease_name", length = 50, nullable = false)
    private String name;
    
    // Getter alias for consistency with database column name
    public String getDiseaseName() {
        return this.name;
    }
    
    // Setter alias for consistency with database column name
    public void setDiseaseName(String diseaseName) {
        this.name = diseaseName;
    }
    
    @Column(name = "symptom_description", columnDefinition = "VARCHAR(MAX)")
    private String symptomDescription;
    
    @Column(name = "cause", columnDefinition = "VARCHAR(MAX)")
    private String cause;
    
    @Column(name = "confidence")
    private Integer confidence;
    
    @Column(name = "example_image_url", columnDefinition = "VARCHAR(MAX)")
    private String exampleImageUrl;
    
    @Column(name = "treatment", columnDefinition = "VARCHAR(MAX)")
    private String treatment;
    
        @Column(name = "recommended_medicines", columnDefinition = "VARCHAR(MAX)")
        private String recommendedMedicines;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;
    
    @OneToMany(mappedBy = "disease", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<PredictionDisease> predictions;
    
    @OneToMany(mappedBy = "disease", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<DiseaseUserImages> userSubmittedImages;
}
