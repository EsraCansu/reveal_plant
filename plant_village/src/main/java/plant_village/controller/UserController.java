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
                            Cookie userInfoCookie = new Cookie("user_info", user.getEmail());
                            userInfoCookie.setPath("/");
                            userInfoCookie.setMaxAge(24 * 60 * 60);
                            response.addCookie(userInfoCookie);
                            
                            Cookie roleCookie = new Cookie("user_role", user.getRole());
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
                    return ResponseEntity.status(401).body("Geçersiz şifre.");
                })
                .orElse(ResponseEntity.status(404).body("Kullanıcı bulunamadı."));
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
        Cookie userInfoCookie = new Cookie("user_info", null);
        userInfoCookie.setPath("/");
        userInfoCookie.setMaxAge(0);
        response.addCookie(userInfoCookie);
        
        Cookie roleCookie = new Cookie("user_role", null);
        roleCookie.setPath("/");
        roleCookie.setMaxAge(0);
        response.addCookie(roleCookie);
        
        return ResponseEntity.ok().body("Logout successful");
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
                return new ResponseEntity<>("Invalid input detected", HttpStatus.BAD_REQUEST);
            }
            
            // Email validation
            if (!xssProtection.isValidEmail(user.getEmail())) {
                return new ResponseEntity<>("Geçersiz email formatı", HttpStatus.BAD_REQUEST);
            }
            
            // Input sanitization
            user.setEmail(xssProtection.sanitize(user.getEmail()));
            user.setUserName(xssProtection.sanitize(user.getUserName()));
            
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

