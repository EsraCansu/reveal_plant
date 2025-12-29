package plant_village.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import plant_village.config.security.JwtAuthenticationFilter;
import plant_village.config.security.RateLimitingFilter;
import plant_village.config.security.SecurityHeadersFilter;
import plant_village.config.security.XssFilter;

/**
 * Security Configuration
 * Configures Spring Security with JWT authentication and various security filters
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final XssFilter xssFilter;
    private final SecurityHeadersFilter securityHeadersFilter;
    
    // BCrypt strength (higher = more secure but slower)
    private static final int BCRYPT_STRENGTH = 12;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // =========================================
            // CORS Configuration
            // =========================================
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // =========================================
            // CSRF Protection
            // =========================================
            // Disabled for REST API (using JWT instead)
            // For production, consider using CSRF tokens with cookies
            .csrf(csrf -> csrf.disable())
            
            // =========================================
            // Session Management
            // =========================================
            // Stateless - no server-side sessions (JWT based)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // =========================================
            // Authorization Rules
            // =========================================
            .authorizeHttpRequests(auth -> auth
                // 📌 Public endpoints - No authentication required
                .requestMatchers(
                    "/api/users/auth/**",           // Login, register, logout
                    "/api/users/auth/login",
                    "/api/users/auth/register",
                    "/api/users/auth/logout"
                ).permitAll()
                
                // 📌 WebSocket endpoints
                .requestMatchers("/ws/**").permitAll()
                
                // 📌 Swagger/OpenAPI documentation
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**"
                ).permitAll()
                
                // 📌 Static resources
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/assets/**",
                    "/favicon.ico"
                ).permitAll()
                
                // 📌 Health check
                .requestMatchers("/actuator/health").permitAll()
                
                // 📌 Prediction endpoint - public for now (can be restricted later)
                .requestMatchers(HttpMethod.POST, "/api/predictions/predict").permitAll()
                
                // 🔐 Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/predictions/approve-images").hasRole("ADMIN")
                
                // 🔐 Authenticated users only
                .requestMatchers("/api/users/profile/**").authenticated()
                .requestMatchers("/api/feedbacks/**").authenticated()
                
                // 🔓 All other endpoints - permitAll for development
                // TODO: Change to .authenticated() in production
                .anyRequest().permitAll()
            )
            
            // =========================================
            // Disable Basic Auth
            // =========================================
            .httpBasic(basic -> basic.disable())
            
            // =========================================
            // Disable Form Login
            // =========================================
            .formLogin(form -> form.disable())
            
            // =========================================
            // Security Filters Chain
            // =========================================
            // Order: SecurityHeaders -> RateLimit -> XSS -> JWT -> UsernamePassword
            .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter, SecurityHeadersFilter.class)
            .addFilterBefore(xssFilter, RateLimitingFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}