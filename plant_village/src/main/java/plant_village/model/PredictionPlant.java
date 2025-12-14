package plant_village.model;
import plant_village.model.*;

import jakarta.persistence.*;

@Entity
@Table(name = "Prediction_Plant")
public class PredictionPlant {

    // Attributes
    // (Surrogate Primary Key)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; 

    // Bire-Çok İlişkisi: Ait olduğu Prediction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    // Constructors
    public PredictionPlant() {
    }

    public PredictionPlant(Prediction prediction, Plant plant) {
        this.prediction = prediction;
        this.plant = plant;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    // Method toString()
    @Override
    public String toString() {
        return "PredictionPlant{" +
                "id=" + id +
                ", predictionId=" + (prediction != null ? prediction.getPredictionId() : "null") +
                ", plantId=" + (plant != null ? plant.getPlantId() : "null") +
                '}';
    }
}