package plant_village.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * STEP 8: User Profile
 * User Profile Request DTO with validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    @Size(min = 2, max = 50, message = "Kullanıcı adı 2-50 karakter arasında olmalıdır")
    @Pattern(regexp = "^[a-zA-Z0-9_ğüşöçıİĞÜŞÖÇ\\s]*$", message = "Kullanıcı adı sadece harf, rakam ve alt çizgi içerebilir")
    private String userName;
    
    @Size(max = 500, message = "Avatar URL maksimum 500 karakter olabilir")
    @Pattern(regexp = "^(https?://.*|data:image/.*|)$", message = "Geçersiz avatar URL formatı")
    private String avatarUrl;
    
    @Size(max = 100, message = "Konum maksimum 100 karakter olabilir")
    private String location;
    
    @Size(max = 500, message = "Bio maksimum 500 karakter olabilir")
    private String bio;
    
    @Pattern(regexp = "^(\\+?[0-9\\s\\-()]*)?$", message = "Geçersiz telefon numarası formatı")
    @Size(max = 20, message = "Telefon numarası maksimum 20 karakter olabilir")
    private String phone;
}
