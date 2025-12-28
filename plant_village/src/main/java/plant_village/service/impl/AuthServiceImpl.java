package plant_village.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
    
    @Value("${jwt.secret:your-very-secure-secret-key-that-is-at-least-256-bits-long-for-HS256-algorithm}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")  // 24 hours in milliseconds
    private long jwtExpiration;
    
    /**
     * Authenticate user with email and password
     * Returns JWT token if successful
     * STEP 1: Authentication - User login
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> {
                log.warn("Login failed - User not found: {}", loginRequest.getEmail());
                return new ResourceNotFoundException("E-mail veya şifre hatalı");
            });
        
        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed - Invalid password for user: {}", loginRequest.getEmail());
            throw new RuntimeException("E-mail veya şifre hatalı");
        }
        
        // Generate token
        String token = generateToken(user);
        
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
        log.info("Registration attempt for email: {}", registerRequest.getEmail());
        
        User newUser = User.builder()
            .email(registerRequest.getEmail())
            .passwordHash(registerRequest.getPassword())  // Will be hashed in UserService
            .userName(registerRequest.getEmail().split("@")[0])  // Username from email prefix
            .role("USER")
            .createAt(LocalDateTime.now())
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
}
