package plant_village.model;
import plant_village.model.*;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Plant")
public class Plant {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_id", unique = true, nullable = false)
    private Integer plantId;

    @Column(name = "plant_name", length = 50, nullable = false)
    private String plantName; 

    @Column(name = "scientific_name", length = 50, nullable = true)
    private String scientificName; 

    @Column(name = "image_url", columnDefinition = "VARCHAR(MAX)", nullable = true)
    private String imageUrl; 

    @Column(columnDefinition = "NTEXT", nullable = true) 
    private String description; 

    @Column(name = "valid_classification", nullable = true)
    private Boolean validClassification; 

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "Prediction_Plant",
            joinColumns = @JoinColumn(name = "plant_id"), // FK for plant table
            inverseJoinColumns = @JoinColumn(name = "prediction_id") // FK for prediction table
    )
    private Set<Prediction> predictions = new HashSet<>();
    
    // plant's has many disease (OneToMany)
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Disease> diseases = new HashSet<>();


    // Constructors
    public Plant() {
    }

    public Plant(Integer plantId, String plantName, String scientificName, String imageUrl, String description, Boolean validClassification) {
        this.plantId = plantId;
    	this.plantName = plantName;
        this.scientificName = scientificName;
        this.imageUrl = imageUrl;
        this.description = description;
        this.validClassification = validClassification; 
        
    }
    
    // Getters and Setters

    public Integer getPlantId() {
        return plantId;
    }

    public void setPlantId(Integer plantId) {
        this.plantId = plantId;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }
    
    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getImageUrl () {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription () {
        return  description;
    }

    public void setDescription(String  description) {
        this. description =  description;
    }
    
    public Boolean getValidClassification () {
        return  validClassification;
    }

    public void setValidClassification(Boolean validClassification) {
        this. validClassification =  validClassification;
    }
       
    public Set<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(Set<Prediction> predictions) {
        this.predictions = predictions;
    }

    public Set<Disease> getDiseases() {
        return diseases;
    }

    public void setDiseases(Set<Disease> diseases) {
        this.diseases = diseases;
    }

    // Method toString()

    @Override
    public String toString() {
    	return "Plant{id=" + plantId + ", name='" + plantName + "'}";
    }
}
