package plant_village.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for Real-time Prediction Updates
 * 
 * Configures STOMP over WebSocket for real-time communication between
 * backend (Spring Boot) and frontend (JavaScript clients).
 * 
 * Endpoints:
 * - /ws/predictions: WebSocket endpoint for client connections
 * - /app/predict: Destination for prediction requests from clients
 * - /topic/predictions: Broadcast destination for all prediction updates
 * - /user/queue/predictions: Individual user prediction results
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Register STOMP endpoints that clients can use to connect.
     * Enables SockJS fallback for browsers that don't support WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("/ws/predictions")
            .setAllowedOrigins(
                "http://localhost:3000",           // Development Frontend
                "http://localhost:8080",           // Development Backend
                "http://localhost",                // Docker container
                "http://127.0.0.1:3000",          // Alternative localhost
                "*"                                // IMPORTANT: Restrict in production
            )
            .withSockJS()                          // Enable SockJS fallback for older browsers
            .setHeartbeatTime(25000)              // Keep-alive heartbeat every 25 seconds
            .setSessionCookieNeeded(true);         // Use cookies for session management
    }

    /**
     * Configure the message broker and destination prefixes.
     * 
     * Message Flow:
     * 1. Client sends message to /app/predict -> handled by @MessageMapping
     * 2. Backend broadcasts to /topic/predictions -> all subscribers receive
     * 3. Backend sends to /user/queue/predictions -> only that user receives
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory message broker
        // For production with multiple instances, use RabbitMQ or ActiveMQ
        registry
            .enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(new long[]{0, 20000});  // Client/server heartbeat

        // Destination prefix for messages sent from client to server
        registry.setApplicationDestinationPrefixes("/app");

        // Destination prefix for user-specific messages (server to client)
        registry.setUserDestinationPrefix("/user");
    }
}
