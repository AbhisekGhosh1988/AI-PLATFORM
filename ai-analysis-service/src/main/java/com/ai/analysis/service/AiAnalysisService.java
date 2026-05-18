package com.ai.analysis.service;

import com.ai.analysis.dto.ServiceAnomalyBatchEvent;
import com.ai.analysis.entity.AiAlertDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final AiPromptBuilder promptBuilder;
    private final AiClientService aiClientService;
    private final AiAlertStorageService storageService;
    public void analyze(ServiceAnomalyBatchEvent event) {
        try {
            // CALLER IS HERE
            String prompt = promptBuilder.build(event);
            log.info("Generated AI prompt");
            // AI MODEL CALL
            List<AiAlertDocument> aiResponse = aiClientService.analyze(prompt);
            log.info("AI Response: {}", aiResponse);
            for (AiAlertDocument item : aiResponse) {
                item.setCreatedAt(Instant.now());
                storageService.save(item);
            }
            log.info("AI analysis completed for service: {}", event.getServiceName());

        } catch (Exception ex) {
            log.error("AI analysis failed", ex);
        }

    }
}
