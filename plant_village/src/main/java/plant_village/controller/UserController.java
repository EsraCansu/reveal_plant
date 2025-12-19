    /**
     * Kullanıcı girişi (login)
     * POST /api/auth/login
     * @param loginRequest {"email": "...", "password": "..."}
     * @return Kullanıcı bilgisi ve rolü (admin/user) veya hata
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody java.util.Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email ve şifre gereklidir.");
        }
        return userService.findByEmail(email)
                .map(user -> {
                    if (userService instanceof plant_village.service.UserServiceImpl) {
                        plant_village.service.UserServiceImpl impl = (plant_village.service.UserServiceImpl) userService;
                        if (impl.verifyPassword(password, user.getPasswordHash())) {
                            java.util.Map<String, Object> result = new java.util.HashMap<>();
                            result.put("id", user.getId());
                            result.put("email", user.getEmail());
                            result.put("role", user.getRole());
                            result.put("name", user.getUserName());
                            return ResponseEntity.ok(result);
                        }
                    }
                    return ResponseEntity.status(401).body("Geçersiz şifre.");
                })
                .orElse(ResponseEntity.status(404).body("Kullanıcı bulunamadı."));
    }
package plant_village.controller;

import plant_village.model.User;
import plant_village.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/users") 
public class UserController {

    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * register the user (POST request from FASTAPI)
     * POST /api/users/register
     * @param user regşster info (in JSON body)
     * @return new user object and HTTP 201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User newUser = userService.registerNewUser(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Kayıt sırasında bir hata oluştu: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * get the user from id (Get request from FastAPI )
     * GET /api/users/{userId}
     * @param userId 
     * @return user instance HTTP 404 Not Found
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); 
    }
}

