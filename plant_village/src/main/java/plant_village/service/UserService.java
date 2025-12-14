package plant_village.service;

import plant_village.model.*;
import java.util.Optional;

public interface UserService {
    
    // record the new register
    User registerNewUser(User user);
    
    // find user with id
    Optional<User> findById(Integer userId);
    
    // find user with email
    Optional<User> findByEmail(String email);
    
    // check the user entity
    boolean checkUsernameExists(String userName);
}