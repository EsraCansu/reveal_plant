package plant_village.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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

        Contact contact = new Contact();
        contact.setName("Reveal Plant Team");
        contact.setEmail("support@revealplant.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Reveal Plant - AI-Powered Plant Disease Detection API")
                .version("1.0.0")
                .contact(contact)
                .description("REST API for plant disease detection using ResNet-101 deep learning model. " +
                        "This API provides endpoints for analyzing plant images, managing user predictions, " +
                        "and accessing real-time predictions via WebSocket.")
                .termsOfService("https://revealplant.com/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
