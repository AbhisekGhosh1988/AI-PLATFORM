package com.ai.platform.logaiplatform.client;

import com.ai.platform.logaiplatform.dto.AnomalyResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnomalyClient {

    private final WebClient anomalyWebClient;

    public List<AnomalyResponseDTO> getAlerts() {
        try {
            return anomalyWebClient.get().
                    uri("/api/alerts").retrieve().
                    bodyToMono(new ParameterizedTypeReference<List<AnomalyResponseDTO>>() {}).
                    timeout(Duration.ofSeconds(10)).block();

        } catch (Exception ex) {
            log.error("Failed to fetch AI alerts", ex);
            return Collections.emptyList();
        }
    }
}
