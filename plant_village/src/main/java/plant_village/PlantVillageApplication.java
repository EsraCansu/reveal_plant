package plant_village;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

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
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║       - REVEAL PLANT - SPRING BOOT BACKEND START -        ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║  - Application started successfully                       ║");
        System.out.println("║  - REST API: http://localhost:8080                        ║");
        System.out.println("║  - WebSocket: ws://localhost:8080/ws/predictions          ║");
        System.out.println("║  -  Database: MS SQL Server (plant_village)               ║");
        System.out.println("║  - ML Server: http://localhost:8000 (FastAPI)             ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }
}
