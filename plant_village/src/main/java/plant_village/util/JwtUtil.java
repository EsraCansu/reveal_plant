package plant_village.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Utility Class
 * Generates and validates JWT tokens for authentication
 */
@Component
public class JwtUtil {

    // Secret key for JWT signing (move to application.properties in production)
    private static final String SECRET_KEY = "revealplant_super_secret_key_2024_this_should_be_very_long_and_secure_at_least_256_bits";
    
    // Token expiration time: 24 hours
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    
    private final SecretKey key;

    public JwtUtil() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generate JWT token for user
     */
    public String generateToken(String email, String role, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return (String) extractClaims(token).get("role");
    }

    /**
     * Extract user ID from token
     */
    public Integer extractUserId(String token) {
        return (Integer) extractClaims(token).get("userId");
    }

    /**
     * Extract all claims from token
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate token without email check
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
