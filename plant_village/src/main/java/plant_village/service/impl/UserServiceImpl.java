package plant_village.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import plant_village.exception.ResourceNotFoundException;
import plant_village.model.User;
import plant_village.repository.UserRepository;
import plant_village.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for User management operations.
 * Handles user registration, authentication, and profile updates.
 * 
 * STEP 1: Authentication - User registration
 * STEP 8: User Profile - User profile management
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user with encrypted password
     * STEP 1: Authentication - New user registration
     */
    @Override
    public User registerNewUser(User user) {
        log.info("Registering new user: {}", user.getUserName());
        
        // Check if username already exists
        if (userRepository.findByUserName(user.getUserName()).isPresent()) {
            log.warn("Username already exists: {}", user.getUserName());
            throw new RuntimeException("Kullanıcı adı zaten mevcut: " + user.getUserName());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Email already exists: {}", user.getEmail());
            throw new RuntimeException("E-mail zaten mevcut: " + user.getEmail());
        }
        
        // Encrypt password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setCreateAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} (ID: {})", savedUser.getUserName(), savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Find user by ID
     * STEP 8: User Profile - Retrieve user profile
     */
    @Override
    public Optional<User> findById(Integer userId) {
        log.info("Fetching user by ID: {}", userId);
        return userRepository.findById(userId);
    }
    
    /**
     * Find user by email
     * Used for authentication
     * STEP 1: Authentication - User lookup
     */
    @Override
    public Optional<User> findByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    /**
     * Check if username exists
     * Used for registration validation
     * STEP 1: Authentication - Username validation
     */
    @Override
    public boolean checkUsernameExists(String userName) {
        log.info("Checking username existence: {}", userName);
        return userRepository.findByUserName(userName).isPresent();
    }
    
    /**
     * Get all users (admin operation)
     * STEP 8: User Profile - Admin list users
     */
    @Override
    public List<User> findAll() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }
    
    /**
     * Update user profile
     * STEP 8: User Profile - User profile update
     */
    @Override
    public User updateUser(User user) {
        log.info("Updating user: {} (ID: {})", user.getUserName(), user.getId());
        
        // Check if user exists
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Kullanıcı bulunamadı - ID: " + user.getId()
            ));
        
        // Update only provided fields
        if (user.getUserName() != null) {
            existingUser.setUserName(user.getUserName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getLocation() != null) {
            existingUser.setLocation(user.getLocation());
        }
        if (user.getBio() != null) {
            existingUser.setBio(user.getBio());
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {} (ID: {})", updatedUser.getUserName(), updatedUser.getId());
        
        return updatedUser;
    }
    
    /**
     * Delete user (admin operation)
     * STEP 8: User Profile - Admin delete user
     */
    @Override
    public void deleteUser(Integer userId) {
        log.info("Deleting user: {}", userId);
        
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            log.warn("User not found for deletion: {}", userId);
            throw new ResourceNotFoundException("Kullanıcı bulunamadı - ID: " + userId);
        }
        
        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }
    
    /**
     * Verify password against stored hash
     * Used for login authentication
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    
    /**
     * Get password encoder for external use
     * Used for password change operations
     */
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
