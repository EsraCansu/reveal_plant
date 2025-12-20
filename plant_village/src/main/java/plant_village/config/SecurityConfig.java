package plant_village.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
    
    private final CorsConfigurationSource corsConfigurationSource;
    
    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource) // CORS'u aktif et
            .and()
            .csrf().disable() // CSRF'yi devre dışı bırak (development için)
            .authorizeRequests()
                .anyRequest().permitAll() // Tüm isteklere izin ver
            .and()
            .httpBasic().disable(); // HTTP Basic Auth'u devre dışı bırak
        
        return http.build();
    }
}