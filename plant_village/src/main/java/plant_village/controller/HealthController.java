package plant_village.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plant_village.service.FastAPIClientService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final FastAPIClientService fastAPIClientService;

    @Value("${spring.application.name:RevealPlant}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean fastApiHealthy = fastAPIClientService.isServerHealthy();
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", fastApiHealthy ? "healthy" : "degraded");
        payload.put("backend", applicationName);
        payload.put("version", applicationVersion);
        payload.put("timestamp", Instant.now().toString());
        payload.put("fastapi_health", fastApiHealthy ? "healthy" : "unhealthy");

        log.info("Health check responded: fastapi={}, status={}", fastApiHealthy, payload.get("status"));
        return ResponseEntity.ok(payload);
    }
}