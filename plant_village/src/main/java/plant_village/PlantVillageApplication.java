package plant_village;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import lombok.extern.slf4j.Slf4j;

/**
 * Reveal Plant - Spring Boot Application
 * 
 * Main entry point for the Plant Disease Recognition Backend
 * 
 * Features:
 * - REST API for user management and predictions
 * - WebSocket real-time prediction updates
 * - Integration with Python FastAPI ML server
 * - MS SQL Server database with Flyway migrations
 * - Spring Security for authentication
 * 
 * Architecture:
 * Frontend (Node.js/Express) → WebSocket → Spring Boot Backend
 *                            → FastAPI ML Server
 *                            → SQL Server Database
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {
    "plant_village.config",
    "plant_village.controller",
    "plant_village.service",
    "plant_village.service.impl",
    "plant_village.repository",
    "plant_village.exception"
})
public class PlantVillageApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantVillageApplication.class, args);
        
        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║       🌿 REVEAL PLANT - SPRING BOOT BACKEND START 🌿       ║");
        log.info("╠═══════════════════════════════════════════════════════════╣");
        log.info("║  ✅ Application started successfully                       ║");
        log.info("║  📡 REST API: http://localhost:8080                        ║");
        log.info("║  🔌 WebSocket: ws://localhost:8080/ws/predictions          ║");
        log.info("║  🗄️  Database: MS SQL Server (plant_village)              ║");
        log.info("║  🔬 ML Server: http://localhost:8000 (FastAPI)             ║");
        log.info("╚═══════════════════════════════════════════════════════════╝");
    }
}
