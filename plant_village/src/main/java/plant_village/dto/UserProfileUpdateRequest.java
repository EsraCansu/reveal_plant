package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STEP 8: User Profile
 * User Profile Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    private String userName;
    private String avatarUrl;
    private String location;
    private String bio;
    private String phone;
}
