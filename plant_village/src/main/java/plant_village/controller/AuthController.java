package plant_village.controller;

import plant_village.model.User;
import plant_village.service.UserService;
import plant_village.util.JwtUtil;
import plant_village.util.XssProtection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Authentication Controller
 * Handles /api/auth/* endpoints for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final XssProtection xssProtection;
    
    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, XssProtection xssProtection) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.xssProtection = xssProtection;
    }

    /**
     * Login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody java.util.Map<String, String> loginRequest,
            HttpServletResponse response) {
        
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        
        // XSS protection
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
                            
                            // Generate JWT Token
                            String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
                            
                            // Set secure cookies
                            Cookie jwtCookie = new Cookie("jwt_token", token);
                            jwtCookie.setHttpOnly(true);
                            jwtCookie.setSecure(false);  // Set to true in production (HTTPS)
                            jwtCookie.setPath("/");
                            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
                            response.addCookie(jwtCookie);
                            
                            // User info cookies (readable by frontend)
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
                            
                            // Response JSON
                            java.util.Map<String, Object> result = new java.util.HashMap<>();
                            result.put("id", user.getId());
                            result.put("email", user.getEmail());
                            result.put("role", user.getRole());
                            result.put("name", user.getUserName());
                            result.put("token", token);
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
     * Get current user from JWT token
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @CookieValue(value = "jwt_token", required = false) String jwtToken) {
        
        try {
            if (jwtToken == null || jwtToken.isEmpty()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Token bulunamadı");
                return ResponseEntity.status(401).body(error);
            }
            
            String email = jwtUtil.extractEmail(jwtToken);
            
            if (email == null || email.isEmpty()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Geçersiz token");
                return ResponseEntity.status(401).body(error);
            }
            
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (!userOpt.isPresent()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Kullanıcı bulunamadı");
                return ResponseEntity.status(404).body(error);
            }
            
            User user = userOpt.get();
            
            // User DTO (without password hash)
            java.util.Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("id", user.getId());
            userData.put("name", user.getUserName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            userData.put("phone", user.getPhone());
            userData.put("location", user.getLocation());
            userData.put("bio", user.getBio());
            userData.put("avatarUrl", user.getAvatarUrl());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("lastLogin", user.getLastLogin());
            
            return ResponseEntity.ok(userData);
            
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Token doğrulanamadı: " + e.getMessage());
            return ResponseEntity.status(401).body(error);
        }
    }

    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        // Clear other cookies
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
}
