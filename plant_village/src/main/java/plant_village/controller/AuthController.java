package plant_village.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import plant_village.dto.ApiResponse;
import plant_village.dto.LoginRequest;
import plant_village.dto.LoginResponse;
import plant_village.model.User;
import plant_village.service.AuthService;
import plant_village.service.UserService;

import jakarta.validation.Valid;

/**
 * Authentication Controller
 * Handles /api/auth/* endpoints for authentication operations
 * 
 * STEP 1: Authentication - Login and registration
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Login endpoint
     * POST /api/auth/login
     * STEP 1: Authentication - User login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login request for email: {}", loginRequest.getEmail());
            
            LoginResponse response = authService.login(loginRequest);
            
            log.info("Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }
    }
    
    /**
     * Register endpoint
     * POST /api/auth/register
     * STEP 1: Authentication - New user registration
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody LoginRequest registerRequest) {
        try {
            log.info("Registration request for email: {}", registerRequest.getEmail());
            
            User newUser = authService.register(registerRequest);
            
            // Generate login response
            LoginResponse response = LoginResponse.builder()
                .userId(newUser.getId())
                .email(newUser.getEmail())
                .userName(newUser.getUserName())
                .role(newUser.getRole())
                .token(authService.generateToken(newUser))
                .build();
            
            log.info("Registration successful for email: {}", registerRequest.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Registration successful"));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }
    
    /**
     * Refresh token endpoint
     * POST /api/auth/refresh
     * STEP 1: Authentication - Refresh JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("Token refresh request");
            
            // Extract token from "Bearer <token>"
            String token = authHeader.replace("Bearer ", "");
            
            // Validate and refresh token
            Integer userId = authService.validateToken(token);
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            String newToken = authService.generateToken(user);
            
            LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole())
                .token(newToken)
                .build();
            
            log.info("Token refreshed successfully for user ID: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }
    }
    
    /**
     * Validate token endpoint
     * GET /api/auth/validate
     * STEP 1: Authentication - Validate JWT token
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("Token validation request");
            
            // Extract token from "Bearer <token>"
            String token = authHeader.replace("Bearer ", "");
            
            // Validate token
            Integer userId = authService.validateToken(token);
            
            log.info("Token validation successful for user ID: {}", userId);
            return ResponseEntity.ok(ApiResponse.success("Token is valid", "Token validated successfully"));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid token"));
        }
    }
}
