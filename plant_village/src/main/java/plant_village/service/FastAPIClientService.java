package plant_village.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import plant_village.model.dto.FastAPIResponse;
import plant_village.model.dto.FastAPIPredictionRequest;

/**
 * FastAPI Client Service
 * 
 * Communicates with Python FastAPI server for plant disease prediction.
 * Handles image submission, processing, and result retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FastAPIClientService {

    @Value("${fastapi.server.url:http://localhost:8000}")
    private String fastApiServerUrl;

    private final RestTemplate restTemplate;

    /**
     * Send prediction request to FastAPI server
     * 
     * @param plantId Plant identifier for context
     * @param imageBase64 Base64 encoded image data
     * @param description User description of plant/symptoms
     * @return FastAPI prediction response with disease probabilities
     */
    public FastAPIResponse predictDisease(Integer plantId, String imageBase64, String description) {
        try {
            log.info("Sending prediction request to FastAPI for plant: {}", plantId);

            String fastApiUrl = fastApiServerUrl + "/predict";

            // Prepare request
            FastAPIPredictionRequest request = FastAPIPredictionRequest.builder()
                    .imageBase64(imageBase64)
                    .imageType(extractImageType(imageBase64))
                    .plantId(plantId)
                    .description(description)
                    .build();

            // Send to FastAPI
            FastAPIResponse response = restTemplate.postForObject(
                    fastApiUrl,
                    request,
                    FastAPIResponse.class
            );

            if (response == null) {
                throw new RuntimeException("FastAPI returned null response");
            }

            log.info("Received prediction from FastAPI: status={}, topPrediction={}",
                    response.getStatus(), response.getTopPrediction());

            return response;

        } catch (Exception e) {
            log.error("Error calling FastAPI prediction service", e);
            throw new RuntimeException("Failed to get prediction from FastAPI: " + e.getMessage(), e);
        }
    }

    /**
     * Get available plants from FastAPI
     */
    public java.util.List<String> getAvailablePlants() {
        try {
            String url = fastApiServerUrl + "/plants";
            return restTemplate.getForObject(url, java.util.List.class);
        } catch (Exception e) {
            log.error("Error fetching available plants from FastAPI", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Get available diseases from FastAPI
     */
    public java.util.List<String> getAvailableDiseases() {
        try {
            String url = fastApiServerUrl + "/diseases";
            return restTemplate.getForObject(url, java.util.List.class);
        } catch (Exception e) {
            log.error("Error fetching available diseases from FastAPI", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Health check for FastAPI server
     */
    public boolean isServerHealthy() {
        try {
            String url = fastApiServerUrl + "/health";
            org.springframework.http.ResponseEntity<String> response = 
                restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("FastAPI server health check failed", e);
            return false;
        }
    }

    /**
     * Extract image type from base64 string
     * Format: data:image/{type};base64,...
     */
    private String extractImageType(String imageBase64) {
        if (imageBase64 == null || !imageBase64.startsWith("data:image/")) {
            return "jpg";
        }
        try {
            int startIndex = imageBase64.indexOf("/") + 1;
            int endIndex = imageBase64.indexOf(";", startIndex);
            return imageBase64.substring(startIndex, endIndex);
        } catch (Exception e) {
            log.warn("Could not extract image type from base64 string", e);
            return "jpg";
        }
    }
}
