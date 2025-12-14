package plant_village.model;
import plant_village.model.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Prediction_Log")
public class PredictionLog {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", unique = true, nullable = false)
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = true)
    private User adminUser; 

    @Column(name = "action_type", nullable = true)
    private Integer actionType;

    @Column(nullable = true)
    private LocalDateTime timestamp;

    @Column(name = "old_value", columnDefinition = "NVARCHAR(MAX)", nullable = true)
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "NVARCHAR(MAX)", nullable = true)
    private String newValue;


    // Constructors
    public PredictionLog() {
    }

    public PredictionLog(Prediction prediction, User adminUser, Integer actionType, LocalDateTime timestamp, String oldValue, String newValue) {
        this.prediction = prediction;
        this.adminUser = adminUser;
        this.actionType = actionType;
        this.timestamp = timestamp;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    // Getters and Setters 
    public Integer getLogId() {
        return logId;
    }
    
    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public User getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    // Method toString()
    @Override
    public String toString() {
        return "PredictionLog{id=" + logId + ", actionType=" + actionType + "}";
    }
}
