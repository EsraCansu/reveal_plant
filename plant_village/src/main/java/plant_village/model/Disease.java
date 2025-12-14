package plant_village.model;
import plant_village.model.*;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Disease")
public class Disease {

    // Attributs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disease_id", unique = true, nullable = false)
    private Integer diseaseId;

    @Column(name = "symptom_description", length = 50, nullable = true)
    private String symptomDescription; 

    @Column(columnDefinition = "NTEXT", nullable = true)
    private String name; 

    @Column(columnDefinition = "NTEXT", nullable = true)
    private String cause; 

    @Column(nullable = true)
    private Float confidence; 

    @Column(name = "example_image_url", columnDefinition = "VARCHAR(MAX)", nullable = true)
    private String exampleImageUrl; 

    @Column(columnDefinition = "NTEXT", nullable = true)
    private String treatment; 

    //one to many between disease and plant .
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    // Disease -> PredictionDisease (disease_id's foreign key in PredictionDisease table)
    @OneToMany(mappedBy = "disease", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PredictionDisease> predictionDiseases = new HashSet<>();


    // Constructors
    public Disease() {
    }
    
    public Disease(String name, String cause, String treatment, Plant plant) {
        this.name = name;
        this.cause = cause;
        this.treatment = treatment;
        this.plant = plant;
    }
    
    // Getters and Setters
    public Integer getDiseaseId() {
        return diseaseId;
    }
    
    // Getter/Setter methods

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Set<PredictionDisease> getPredictionDiseases() {
        return predictionDiseases;
    }

    public void setPredictionDiseases(Set<PredictionDisease> predictionDiseases) {
        this.predictionDiseases = predictionDiseases;
    }

    // Method toString()
    @Override
    public String toString() {
        return "Disease{id=" + diseaseId + ", name='" + name + "'}";
    }
}