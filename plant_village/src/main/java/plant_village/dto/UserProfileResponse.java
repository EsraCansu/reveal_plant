package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * STEP 8: User Profile
 * User Profile Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Integer userId;
    private String userName;
    private String email;
    private LocalDateTime createAt;
    private LocalDateTime lastLogin;
    private String role;
    private String avatarUrl;
    private String location;
    private String bio;
    private Boolean isActive;
    private String phone;
    
    private String message;
}
