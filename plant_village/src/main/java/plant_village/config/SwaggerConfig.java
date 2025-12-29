package plant_village.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI plantVillageAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        Server productionServer = new Server();
        productionServer.setUrl("https://api.revealplant.com");
        productionServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setName("Reveal Plant Team");
        contact.setEmail("support@revealplant.com");
        contact.setUrl("https://revealplant.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Reveal Plant - AI-Powered Plant Disease Detection API")
                .version("1.0.0")
                .contact(contact)
                .description("REST API for plant disease detection using ResNet-101 deep learning model. " +
                        "This API provides endpoints for analyzing plant images, managing user predictions, " +
                        "authenticating users, and accessing real-time predictions via WebSocket.")
                .termsOfService("https://revealplant.com/terms")
                .license(license);

        // Security Scheme - JWT Bearer Token
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token for authentication"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, productionServer))
                .components(components);
    }
}
