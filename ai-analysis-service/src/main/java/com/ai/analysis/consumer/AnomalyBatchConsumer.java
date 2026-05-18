package com.ai.analysis.consumer;

import com.ai.analysis.dto.ServiceAnomalyBatchEvent;
import com.ai.analysis.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnomalyBatchConsumer {

    private final AiAnalysisService aiAnalysisService;

    @KafkaListener(topics = "anomaly-batch-events", groupId = "ai-analysis-group")
    public void consume(ServiceAnomalyBatchEvent event) {
        log.info("Received event for service: {}", event.getServiceName());
        aiAnalysisService.analyze(event);
    }
}
