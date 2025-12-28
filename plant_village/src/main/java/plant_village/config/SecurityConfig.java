package plant_village.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
    
    private final CorsConfigurationSource corsConfigurationSource;
    
    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // ✅ Public endpoints - Authentication gerektirmez
                .requestMatchers(
                    "/api/users/auth/**",      // Login, register, logout endpoints
                    "/ws/**",                   // WebSocket endpoints
                    "/api/predictions/predict"  // Geçici olarak açık
                ).permitAll()
                
                // ✅ Tüm diğer endpoint'ler şimdilik permitAll
                // Güvenlik sonra sıkılaştırılacak
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> basic.disable());
        
        return http.build();
    }
}