package plant_village.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plant_village.model.PlantUserImages;

import java.util.List;

/**
 * Repository interface for PlantUserImages entity.
 * Manages user-submitted plant images from positive feedback.
 */
@Repository
public interface PlantUserImagesRepository extends JpaRepository<PlantUserImages, Integer> {
    
    /**
     * Find all images for a specific plant
     */
    List<PlantUserImages> findByPlantId(Integer plantId);
    
    /**
     * Find all images submitted by a specific user
     */
    List<PlantUserImages> findBySubmittedByUser_Id(Integer userId);
    
    /**
     * Find verified images only
     */
    List<PlantUserImages> findByVerifiedTrue();
    
    /**
     * Check if image URL already exists for a plant (avoid duplicates)
     */
    boolean existsByPlantIdAndImageUrl(Integer plantId, String imageUrl);
    
    /**
     * Count images contributed by a user
     */
    long countBySubmittedByUser_Id(Integer userId);
    
    /**
     * Count images for a plant
     */
    long countByPlantId(Integer plantId);
}
