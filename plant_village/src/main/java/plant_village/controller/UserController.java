package plant_village.controller;

import plant_village.model.User;
import plant_village.model.Prediction;
import plant_village.repository.PredictionRepository;
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
import java.util.List;

@RestController
@RequestMapping("/api/users") 
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final XssProtection xssProtection;
    private final PredictionRepository predictionRepository;
    
    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, XssProtection xssProtection, PredictionRepository predictionRepository) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.xssProtection = xssProtection;
        this.predictionRepository = predictionRepository;
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
                    if (userService instanceof plant_village.service.impl.UserServiceImpl) {
                        plant_village.service.impl.UserServiceImpl impl = (plant_village.service.impl.UserServiceImpl) userService;
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
                            
                            // ✅ User ID cookie'si ekle
                            Cookie userIdCookie = new Cookie("userId", String.valueOf(user.getId()));
                            userIdCookie.setPath("/");
                            userIdCookie.setMaxAge(24 * 60 * 60);
                            response.addCookie(userIdCookie);
                            
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
     * ✅ YENİ: Get current user from JWT token
     * GET /api/auth/me
     */
    @GetMapping("/auth/me")
    public ResponseEntity<?> getCurrentUser(
            @CookieValue(value = "jwt_token", required = false) String jwtToken) {
        
        try {
            // Token kontrolü
            if (jwtToken == null || jwtToken.isEmpty()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Token bulunamadı");
                return ResponseEntity.status(401).body(error);
            }
            
            // Token'dan email çıkar
            String email = jwtUtil.extractEmail(jwtToken);
            
            if (email == null || email.isEmpty()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Geçersiz token");
                return ResponseEntity.status(401).body(error);
            }
            
            // Kullanıcıyı bul
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (!userOpt.isPresent()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Kullanıcı bulunamadı");
                return ResponseEntity.status(404).body(error);
            }
            
            User user = userOpt.get();
            
            // User DTO oluştur (password hash'i gönderme!)
            java.util.Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("id", user.getId());
            userData.put("name", user.getUserName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            userData.put("phone", user.getPhone());
            userData.put("location", user.getLocation());
            userData.put("bio", user.getBio());
            userData.put("avatarUrl", user.getAvatarUrl());
            userData.put("createdAt", user.getCreateAt());
            userData.put("lastLogin", user.getLastLogin());
            
            return ResponseEntity.ok(userData);
            
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Token doğrulanamadı: " + e.getMessage());
            return ResponseEntity.status(401).body(error);
        }
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
        
        // userId cookie'sini sil
        Cookie userIdCookie = new Cookie("userId", null);
        userIdCookie.setPath("/");
        userIdCookie.setMaxAge(0);
        response.addCookie(userIdCookie);
        
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
     * Get all users (Admin only)
     * GET /api/users/all
     * @return list of all users
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            java.util.List<User> users = userService.findAll();
            
            // Password'ları çıkar (güvenlik)
            java.util.List<java.util.Map<String, Object>> sanitizedUsers = users.stream()
                .map(user -> {
                    java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("userName", user.getUserName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    userMap.put("createdAt", user.getCreateAt());
                    userMap.put("lastLogin", user.getLastLogin());
                    return userMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(sanitizedUsers);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Kullanıcılar yüklenemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Update user role (Admin only)
     * PUT /api/users/{userId}/role
     * @param userId User ID
     * @param request Contains new role
     * @return updated user
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Integer userId,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String newRole = request.get("role");
            
            if (newRole == null || (!newRole.equals("USER") && !newRole.equals("ADMIN"))) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Geçersiz rol. USER veya ADMIN olmalı.");
                return ResponseEntity.badRequest().body(error);
            }
            
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Kullanıcı bulunamadı");
                return ResponseEntity.status(404).body(error);
            }
            
            User user = userOpt.get();
            user.setRole(newRole);
            User updatedUser = userService.updateUser(user);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "Rol güncellendi");
            result.put("userId", updatedUser.getId());
            result.put("newRole", updatedUser.getRole());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Rol güncellenemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Delete user (Admin only)
     * DELETE /api/users/{userId}
     * @param userId User ID to delete
     * @return success message
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        try {
            userService.deleteUser(userId);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "Kullanıcı silindi");
            result.put("userId", userId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Kullanıcı silinemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
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
            result.put("createdAt", user.getCreateAt());
            result.put("lastLogin", user.getLastLogin());
            // Profile fields
            result.put("phone", user.getPhone());
            result.put("location", user.getLocation());
            result.put("bio", user.getBio());
            result.put("avatarUrl", user.getAvatarUrl());
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
    
    /**
     * Update user profile
     * PUT /api/users/profile
     * @param profileRequest Contains profile fields to update
     * @return updated user info
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody java.util.Map<String, String> profileRequest) {
        try {
            String email = profileRequest.get("email");
            
            if (email == null) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Email gereklidir");
                return ResponseEntity.badRequest().body(error);
            }
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (!userOpt.isPresent()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Kullanıcı bulunamadı");
                return ResponseEntity.status(404).body(error);
            }
            
            User user = userOpt.get();
            
            // Update fields if provided
            if (profileRequest.containsKey("userName")) {
                String userName = xssProtection.sanitize(profileRequest.get("userName"));
                user.setUserName(userName);
            }
            
            if (profileRequest.containsKey("phone")) {
                user.setPhone(xssProtection.sanitize(profileRequest.get("phone")));
            }
            
            if (profileRequest.containsKey("location")) {
                user.setLocation(xssProtection.sanitize(profileRequest.get("location")));
            }
            
            if (profileRequest.containsKey("bio")) {
                user.setBio(xssProtection.sanitize(profileRequest.get("bio")));
            }
            
            if (profileRequest.containsKey("avatarUrl")) {
                user.setAvatarUrl(profileRequest.get("avatarUrl"));
            }
            
            User updatedUser = userService.updateUser(user);
            
            // Return sanitized response
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "Profile updated successfully");
            result.put("user", java.util.Map.of(
                "id", updatedUser.getId(),
                "userName", updatedUser.getUserName(),
                "email", updatedUser.getEmail(),
                "phone", updatedUser.getPhone() != null ? updatedUser.getPhone() : "",
                "location", updatedUser.getLocation() != null ? updatedUser.getLocation() : "",
                "bio", updatedUser.getBio() != null ? updatedUser.getBio() : "",
                "avatarUrl", updatedUser.getAvatarUrl() != null ? updatedUser.getAvatarUrl() : "",
                "role", updatedUser.getRole(),
                "createdAt", updatedUser.getCreateAt()
            ));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Profile güncellenemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Change user password
     * PUT /api/users/password
     * @param passwordRequest Contains email, currentPassword, newPassword
     * @return success message
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody java.util.Map<String, String> passwordRequest) {
        try {
            String email = passwordRequest.get("email");
            String currentPassword = passwordRequest.get("currentPassword");
            String newPassword = passwordRequest.get("newPassword");
            
            if (email == null || currentPassword == null || newPassword == null) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Email, current password ve new password gereklidir");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (newPassword.length() < 6) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Şifre en az 6 karakter olmalıdır");
                return ResponseEntity.badRequest().body(error);
            }
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (!userOpt.isPresent()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Kullanıcı bulunamadı");
                return ResponseEntity.status(404).body(error);
            }
            
            User user = userOpt.get();
            
            // Verify current password
            if (userService instanceof plant_village.service.impl.UserServiceImpl) {
                plant_village.service.impl.UserServiceImpl impl = (plant_village.service.impl.UserServiceImpl) userService;
                if (!impl.verifyPassword(currentPassword, user.getPasswordHash())) {
                    java.util.Map<String, String> error = new java.util.HashMap<>();
                    error.put("error", "Mevcut şifre yanlış");
                    return ResponseEntity.status(401).body(error);
                }
                
                // Hash and update new password using BCrypt
                String newPasswordHash = impl.getPasswordEncoder().encode(newPassword);
                user.setPasswordHash(newPasswordHash);
                userService.updateUser(user);
                
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("message", "Şifre başarıyla güncellendi");
                
                return ResponseEntity.ok(result);
            }
            
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Şifre güncellenemedi");
            return ResponseEntity.status(500).body(error);
            
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Şifre güncellenemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get user statistics (diagnoses count, etc.)
     * GET /api/users/{userId}/stats
     * @param userId User ID
     * @return user statistics
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Integer userId) {
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                java.util.Map<String, String> error = new java.util.HashMap<>();
                error.put("error", "Kullanıcı bulunamadı");
                return ResponseEntity.status(404).body(error);
            }
            
            User user = userOpt.get();
            
            // Get user's predictions from repository
            List<Prediction> userPredictions = predictionRepository.findByUserId(userId);
            
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("userId", user.getId());
            stats.put("memberSince", user.getCreateAt());
            stats.put("lastLogin", user.getLastLogin());
            stats.put("totalDiagnoses", userPredictions.size());
            
            // Get last diagnosis date if exists
            if (!userPredictions.isEmpty()) {
                java.time.LocalDateTime lastDiagnosisDate = userPredictions.stream()
                    .map(p -> p.getCreateAt())
                    .filter(date -> date != null)
                    .max(java.time.LocalDateTime::compareTo)
                    .orElse(null);
                stats.put("lastDiagnosis", lastDiagnosisDate);
            } else {
                stats.put("lastDiagnosis", null);
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "İstatistikler yüklenemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Upload user avatar
     * POST /api/users/avatar
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestBody java.util.Map<String, String> request) {
        try {
            String email = request.get("email");
            String base64Image = request.get("avatar");
            
            if (email == null || base64Image == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Email ve avatar gerekli"));
            }
            
            // Remove data:image/...;base64, prefix if exists
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }
            
            // Decode base64
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            
            // Create uploads directory if not exists
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads", "avatars");
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }
            
            // Generate unique filename
            String fileName = email.replace("@", "_").replace(".", "_") + "_" + 
                            System.currentTimeMillis() + ".jpg";
            java.nio.file.Path filePath = uploadDir.resolve(fileName);
            
            // Save file
            java.nio.file.Files.write(filePath, imageBytes);
            
            // Update user's avatar_url
            User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
            
            user.setAvatarUrl("/uploads/avatars/" + fileName);
            userService.updateUser(user);
            
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("message", "Avatar başarıyla yüklendi");
            response.put("avatarUrl", user.getAvatarUrl());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Avatar yüklenemedi: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

