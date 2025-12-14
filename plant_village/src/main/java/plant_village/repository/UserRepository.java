package plant_village.repository;
import plant_village.model.*;

import plant_village.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // automatic created from Spring Data JPA 
    Optional<User> findByEmail(String email);
    
    // register name control at registering moment
    boolean existsByUserName(String userName);
    
    // findById(Integer) 
    Optional<User> findByUserId(Integer userId);
}
