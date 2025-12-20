package plant_village.controller;

import plant_village.model.User;
import plant_village.service.UserService;
import plant_village.util.JwtUtil;
import plant_village.util.XssProtection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/api/users") 
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final XssProtection xssProtection;
    
    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, XssProtection xssProtection) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.xssProtection = xssProtection;
    }

    /**
     * ✅ GÜNCELLEME: JWT Token ve Secure Cookie ile Login
     * POST /api/auth/login
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(
            @RequestBody java.util.Map<String, String> loginRequest,
            HttpServletResponse response) {
        
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        
        // XSS koruması
        if (xssProtection.isDangerous(email) || xssProtection.isDangerous(password)) {
            return ResponseEntity.badRequest().body("Invalid input detected");
        }
        
        // Validation
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email ve şifre gereklidir.");
        }
        
        if (!xssProtection.isValidEmail(email)) {
            return ResponseEntity.badRequest().body("Geçersiz email formatı.");
        }
        
        return userService.findByEmail(email)
                .map(user -> {
                    if (userService instanceof plant_village.service.UserServiceImpl) {
                        plant_village.service.UserServiceImpl impl = (plant_village.service.UserServiceImpl) userService;
                        if (impl.verifyPassword(password, user.getPasswordHash())) {
                            
                            // ✅ JWT Token oluştur
                            String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
                            
                            // ✅ Secure Cookie ekle
                            Cookie jwtCookie = new Cookie("jwt_token", token);
                            jwtCookie.setHttpOnly(true);  // XSS koruması
                            jwtCookie.setSecure(false);    // Production'da true olmalı (HTTPS)
                            jwtCookie.setPath("/");
                            jwtCookie.setMaxAge(24 * 60 * 60); // 24 saat
                            response.addCookie(jwtCookie);
                            
                            // Kullanıcı bilgisi için ayrı cookie (frontend'de okunabilir)
                            // Cookie değerlerini URL encode et (boşluk ve özel karakterler için)
                            String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
                            String encodedName = URLEncoder.encode(user.getUserName(), StandardCharsets.UTF_8);
                            String encodedRole = URLEncoder.encode(user.getRole(), StandardCharsets.UTF_8);
                            
                            Cookie userEmailCookie = new Cookie("userEmail", encodedEmail);
                            userEmailCookie.setPath("/");
                            userEmailCookie.setMaxAge(24 * 60 * 60);
                            response.addCookie(userEmailCookie);
                            
                            Cookie userNameCookie = new Cookie("userName", encodedName);
                            userNameCookie.setPath("/");
                            userNameCookie.setMaxAge(24 * 60 * 60);
                            response.addCookie(userNameCookie);
                            
                            Cookie roleCookie = new Cookie("userRole", encodedRole);
                            roleCookie.setPath("/");
                            roleCookie.setMaxAge(24 * 60 * 60);
                            response.addCookie(roleCookie);
                            
                            // Response
                            java.util.Map<String, Object> result = new java.util.HashMap<>();
                            result.put("id", user.getId());
                            result.put("email", user.getEmail());
                            result.put("role", user.getRole());
                            result.put("name", user.getUserName());
                            result.put("token", token); // İsteğe bağlı: frontend'de de kullanılabilir
                            result.put("message", "Login successful");
                            
                            return ResponseEntity.ok(result);
                        }
                    }
                    java.util.Map<String, String> error = new java.util.HashMap<>();
                    error.put("error", "Geçersiz şifre");
                    return ResponseEntity.status(401).body(error);
                })
                .orElseGet(() -> {
                    java.util.Map<String, String> error = new java.util.HashMap<>();
                    error.put("error", "Kullanıcı bulunamadı");
                    return ResponseEntity.status(404).body(error);
                });
    }

    /**
     * ✅ YENİ: Logout endpoint - Cookie'leri temizle
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // JWT cookie'yi sil
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        // Diğer cookie'leri sil
        Cookie userEmailCookie = new Cookie("userEmail", null);
        userEmailCookie.setPath("/");
        userEmailCookie.setMaxAge(0);
        response.addCookie(userEmailCookie);
        
        Cookie userNameCookie = new Cookie("userName", null);
        userNameCookie.setPath("/");
        userNameCookie.setMaxAge(0);
        response.addCookie(userNameCookie);
        
        Cookie roleCookie = new Cookie("userRole", null);
        roleCookie.setPath("/");
        roleCookie.setMaxAge(0);
        response.addCookie(roleCookie);
        
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("message", "Logout successful");
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ GÜNCELLEME: XSS korumalı kullanıcı kaydı
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // XSS koruması
            if (xssProtection.isDangerous(user.getEmail()) || 
                xssProtection.isDangerous(user.getUserName())) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Invalid input detected");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            
            // Email validation
            if (!xssProtection.isValidEmail(user.getEmail())) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Geçersiz email formatı");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            
            // Input sanitization
            user.setEmail(xssProtection.sanitize(user.getEmail()));
            user.setUserName(xssProtection.sanitize(user.getUserName()));
            
            User newUser = userService.registerNewUser(user);
            
            // Success response
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Kayıt başarılı");
            response.put("user", newUser);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Kayıt sırasında bir hata oluştu: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get user by email (for frontend auth)
     * GET /api/users/by-email?email={email}
     * @param email User email
     * @return user instance or HTTP 404 Not Found
     */
    @GetMapping("/by-email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Password'u response'a dahil etme (güvenlik)
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("id", user.getId());
            result.put("email", user.getEmail());
            result.put("userName", user.getUserName());
            result.put("role", user.getRole());
            result.put("createdAt", user.getCreatedAt());
            result.put("lastLogin", user.getLastLogin());
            return ResponseEntity.ok(result);
        } else {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Kullanıcı bulunamadı");
            return ResponseEntity.status(404).body(error);
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

