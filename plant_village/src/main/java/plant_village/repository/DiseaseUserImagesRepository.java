package plant_village.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plant_village.model.DiseaseUserImages;

import java.util.List;

/**
 * Repository interface for DiseaseUserImages entity.
 * Manages user-submitted disease images from positive feedback.
 */
@Repository
public interface DiseaseUserImagesRepository extends JpaRepository<DiseaseUserImages, Integer> {
    
    /**
     * Find all images for a specific disease
     */
    List<DiseaseUserImages> findByDiseaseId(Integer diseaseId);
    
    /**
     * Find all images submitted by a specific user
     */
    List<DiseaseUserImages> findBySubmittedByUser_Id(Integer userId);
    
    /**
     * Find verified images only
     */
    List<DiseaseUserImages> findByVerifiedTrue();
    
    /**
     * Check if image URL already exists for a disease (avoid duplicates)
     */
    boolean existsByDiseaseIdAndImageUrl(Integer diseaseId, String imageUrl);
    
    /**
     * Count images contributed by a user
     */
    long countBySubmittedByUser_Id(Integer userId);
    
    /**
     * Count images for a disease
     */
    long countByDiseaseId(Integer diseaseId);
}
