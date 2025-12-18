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
    
    @Column(name = "disease_name", length = 50)
    private String name;
    
    @Column(name = "symptom_description")
    private String symptomDescription;
    
    @Column(name = "cause")
    private String cause;
    
    @Column(name = "confidence")
    private Integer confidence;
    
    @Column(name = "example_image_url")
    private String exampleImageUrl;
    
    @Column(name = "treatment")
    private String treatment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;
}
