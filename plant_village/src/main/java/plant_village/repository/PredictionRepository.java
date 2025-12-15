package plant_village.repository;

import plant_village.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Integer> {

    // user prediction - newest predictions first
    List<Prediction> findByUser_IdOrderByCreatedAtDesc(Integer userId);

    // findById is already provided by JpaRepository<Prediction, Integer>
    // Optional<Prediction> findById(Long id); // inherited

    // admin control
    List<Prediction> findByIsValid(Boolean isValid);
}