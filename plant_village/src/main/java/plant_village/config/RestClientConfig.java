package plant_village.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * REST Client Configuration
 * 
 * Configures RestTemplate bean for HTTP communication with FastAPI server
 */
@Configuration
public class RestClientConfig {

    /**
     * Create RestTemplate bean with custom configuration
     * - Connection timeout: 10 seconds
     * - Read timeout: 30 seconds (for long ML model processing)
     * - Automatic error handling
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}
