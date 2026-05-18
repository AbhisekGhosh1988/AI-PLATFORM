package com.ai.analysis.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Abstract base class for HTTP-based AI service clients.
 * Encapsulates common HTTP request/response handling logic.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAIClient {
    private final RestTemplate restTemplate;

    /**
     * Sends an HTTP POST request to an AI service with Bearer token authentication.
     *
     * @param url The API endpoint URL
     * @param apiKey The API key for Bearer authentication
     * @param requestPayload The request object (will be serialized to JSON)
     * @return The response body as a Map
     */
    protected ResponseEntity<Map> sendRequest(String url, String apiKey, Object requestPayload) {
        try {
            // Build headers
            HttpHeaders headers = buildHeaders(apiKey);
            // Build HTTP entity
            HttpEntity<Object> entity = new HttpEntity<>(requestPayload, headers);
            // Log request
            log.debug("Sending request to: {} | Model: {}", url, getServiceName());
            // Execute request
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,
                    entity, Map.class);
            // Validate response
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[{}] Response received successfully", getServiceName());
                return response;
            } else {
                log.error("[{}] Unexpected status: {}", getServiceName(), response.getStatusCode());
                throw new RuntimeException("API call failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("[{}] Failed to send request to {}", getServiceName(), url, e);
            throw new RuntimeException("Failed to communicate with " + getServiceName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Builds HTTP headers with Bearer authentication and JSON content type.
     *
     * @param apiKey The API key
     * @return Configured HttpHeaders
     */
    private HttpHeaders buildHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        addCustomHeaders(headers);  // Allow subclasses to add custom headers
        return headers;
    }

    /**
     * Hook method for subclasses to add custom headers if needed.
     * Override this if your AI service requires additional headers.
     *
     * @param headers The HTTP headers to customize
     */
    protected void addCustomHeaders(HttpHeaders headers) {
        // Default implementation - subclasses can override
    }

    /**
     * Returns the name of the AI service (for logging and identification).
     * Must be implemented by subclasses.
     *
     * @return The service name
     */
    protected abstract String getServiceName();

    /**
     * Extracts the AI response text from the response map.
     * Must be implemented by subclasses as different services have different response formats.
     *
     * @param responseBody The response body from the API
     * @return The extracted response text
     */
    protected abstract String extractResponse(Map responseBody);

    /**
     * Sends a request and extracts the response in one call.
     *
     * @param url The API endpoint URL
     * @param apiKey The API key
     * @param requestPayload The request payload
     * @return The extracted response text
     */
    public String sendAndExtract(String url, String apiKey, Object requestPayload) {
        ResponseEntity<Map> response = sendRequest(url, apiKey, requestPayload);
        return extractResponse(response.getBody());
    }
}
