package plant_village.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import plant_village.config.security.PasswordValidator;
import plant_village.dto.LoginRequest;
import plant_village.dto.LoginResponse;
import plant_village.exception.ResourceNotFoundException;
import plant_village.model.User;
import plant_village.repository.UserRepository;
import plant_village.service.AuthService;
import plant_village.service.UserService;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Authentication operations.
 * Handles JWT token generation, validation, and user authentication.
 * 
 * STEP 1: Authentication
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordValidator passwordValidator;
    
    @Value("${jwt.secret:your-very-secure-secret-key-that-is-at-least-256-bits-long-for-HS256-algorithm}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")  // 24 hours in milliseconds
    private long jwtExpiration;
    
    // Failed login tracking for brute force protection
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes
    private final java.util.Map<String, FailedLoginTracker> failedLogins = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * Authenticate user with email and password
     * Returns JWT token if successful
     * STEP 1: Authentication - User login
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().toLowerCase().trim();
        log.info("Login attempt for email: {}", email);
        
        // Check for account lockout
        if (isAccountLocked(email)) {
            log.warn("Login blocked - Account temporarily locked: {}", email);
            throw new RuntimeException("Çok fazla başarısız giriş denemesi. Lütfen 15 dakika sonra tekrar deneyin.");
        }
        
        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                recordFailedLogin(email);
                log.warn("Login failed - User not found: {}", email);
                return new ResourceNotFoundException("E-mail veya şifre hatalı");
            });
        
        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            recordFailedLogin(email);
            log.warn("Login failed - Invalid password for user: {}", email);
            throw new RuntimeException("E-mail veya şifre hatalı");
        }
        
        // Clear failed login attempts on success
        clearFailedLogins(email);
        
        // Generate token
        String token = generateToken(user);
        
        // Update last login time
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User logged in successfully: {} (ID: {})", user.getEmail(), user.getId());
        
        return LoginResponse.builder()
            .userId(user.getId())
            .token(token)
            .role(user.getRole())
            .email(user.getEmail())
            .userName(user.getUserName())
            .build();
    }
    
    /**
     * Register new user
     * STEP 1: Authentication - New user registration
     */
    @Override
    public User register(LoginRequest registerRequest) {
        String email = registerRequest.getEmail().toLowerCase().trim();
        log.info("Registration attempt for email: {}", email);
        
        // Validate password strength
        List<String> passwordErrors = passwordValidator.validate(registerRequest.getPassword());
        if (!passwordErrors.isEmpty()) {
            String errorMessage = String.join(", ", passwordErrors);
            log.warn("Registration failed - Weak password for email: {}", email);
            throw new RuntimeException("Şifre gereksinimleri karşılanmıyor: " + errorMessage);
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Registration failed - Email already exists: {}", email);
            throw new RuntimeException("Bu e-mail adresi zaten kayıtlı");
        }
        
        User newUser = User.builder()
            .email(registerRequest.getEmail())
            .passwordHash(registerRequest.getPassword())  // Will be hashed in UserService
            .userName(registerRequest.getEmail().split("@")[0])  // Username from email prefix
            .role("USER")
            .lastLogin(LocalDateTime.now())
            .build();
        
        User registeredUser = userService.registerNewUser(newUser);
        
        log.info("User registered successfully: {} (ID: {})", registeredUser.getEmail(), registeredUser.getId());
        
        return registeredUser;
    }
    
    /**
     * Generate JWT token for authenticated user
     * Token includes user ID, email, username, and role
     */
    @Override
    public String generateToken(User user) {
        log.info("Generating JWT token for user: {}", user.getEmail());
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        String token = Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("userName", user.getUserName())
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
        
        log.info("JWT token generated for user: {}", user.getEmail());
        
        return token;
    }
    
    /**
     * Validate JWT token
     * Returns user ID if valid, throws exception if invalid
     */
    @Override
    public Integer validateToken(String token) {
        log.info("Validating JWT token");
        
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            String userIdString = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
            
            log.info("Token validated successfully for user ID: {}", userIdString);
            
            return Integer.parseInt(userIdString);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Geçersiz token: " + e.getMessage());
        }
    }
    
    /**
     * Refresh JWT token
     * Returns new token with extended expiration
     */
    @Override
    public String refreshToken(String token) {
        log.info("Refreshing JWT token");
        
        try {
            // Validate current token
            Integer userId = validateToken(token);
            
            // Get user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı - ID: " + userId));
            
            // Generate new token
            String newToken = generateToken(user);
            
            log.info("Token refreshed successfully for user ID: {}", userId);
            
            return newToken;
        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token yenileme başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Get token expiration time in milliseconds
     */
    @Override
    public long getTokenExpiration() {
        return jwtExpiration;
    }
    
    // =========================================
    // BRUTE FORCE PROTECTION METHODS
    // =========================================
    
    /**
     * Record a failed login attempt
     */
    private void recordFailedLogin(String email) {
        failedLogins.compute(email, (key, tracker) -> {
            if (tracker == null || System.currentTimeMillis() - tracker.firstFailedTime > LOCKOUT_DURATION_MS) {
                return new FailedLoginTracker(System.currentTimeMillis(), 1);
            }
            tracker.failedCount++;
            return tracker;
        });
    }
    
    /**
     * Check if account is locked due to too many failed attempts
     */
    private boolean isAccountLocked(String email) {
        FailedLoginTracker tracker = failedLogins.get(email);
        if (tracker == null) {
            return false;
        }
        
        // Check if lockout period has expired
        if (System.currentTimeMillis() - tracker.firstFailedTime > LOCKOUT_DURATION_MS) {
            failedLogins.remove(email);
            return false;
        }
        
        return tracker.failedCount >= MAX_FAILED_ATTEMPTS;
    }
    
    /**
     * Clear failed login attempts for an email
     */
    private void clearFailedLogins(String email) {
        failedLogins.remove(email);
    }
    
    /**
     * Helper class to track failed login attempts
     */
    private static class FailedLoginTracker {
        long firstFailedTime;
        int failedCount;
        
        FailedLoginTracker(long firstFailedTime, int failedCount) {
            this.firstFailedTime = firstFailedTime;
            this.failedCount = failedCount;
        }
    }
}
