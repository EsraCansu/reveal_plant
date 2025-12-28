package plant_village.service;

import plant_village.model.User;
import plant_village.dto.LoginRequest;
import plant_village.dto.LoginResponse;

/**
 * Service interface for Authentication operations.
 * Handles user login, token generation, and token validation.
 * 
 * STEP 1: Authentication
 */
public interface AuthService {
    
    /**
     * Authenticate user with email and password
     * Returns JWT token if successful
     * STEP 1: Authentication - User login
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * Register new user
     * STEP 1: Authentication - New user registration
     */
    User register(LoginRequest registerRequest);
    
    /**
     * Generate JWT token for authenticated user
     * Token includes user ID, email, and role
     */
    String generateToken(User user);
    
    /**
     * Validate JWT token
     * Returns user ID if valid, throws exception if invalid
     */
    Integer validateToken(String token);
    
    /**
     * Refresh JWT token
     * Returns new token with extended expiration
     */
    String refreshToken(String token);
    
    /**
     * Get token expiration time in milliseconds
     */
    long getTokenExpiration();
}
