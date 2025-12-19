package plant_village.controller;

import plant_village.model.User;
import plant_village.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Bu sınıfın HTTP isteklerini işleyeceğini belirtir
@RequestMapping("/api/users") // Temel yol: /api/users
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    // Servis katmanını enjekte ediyoruz
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
            } catch (Exception e) {
                // Hata mesajını düz yazı değil, JSON objesi olarak dönüyoruz:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of("message", e.getMessage()));
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
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK)) // Bulunursa 200 OK döndür
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Bulunamazsa 404 Not Found döndür
    }
    
    // Not: Giriş (Login) metodu burada veya ayrı bir AuthController'da yer almalıdır.
}