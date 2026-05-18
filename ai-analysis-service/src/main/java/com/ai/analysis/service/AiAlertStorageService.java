package com.ai.analysis.service;

import com.ai.analysis.entity.AiAlertDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAlertStorageService {

    private final OpenSearchClient openSearchClient;
    public void save(AiAlertDocument alert) {
        try {
            openSearchClient.index(i -> i.index("ai-alerts").
                    document(alert));
            log.info("Saved AI alert: {}", alert.getException());

        } catch (Exception ex) {
            log.error("Failed to save AI alert", ex);
        }
    }
}
