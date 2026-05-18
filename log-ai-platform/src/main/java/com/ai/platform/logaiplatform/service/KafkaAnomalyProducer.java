package com.ai.platform.logaiplatform.service;

import com.ai.platform.logaiplatform.dto.ServiceAnomalyBatchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAnomalyProducer {

    private static final String TOPIC =
            "anomaly-batch-events";

    private final KafkaTemplate<
            String,
            ServiceAnomalyBatchEvent> kafkaTemplate;

    public void publish(
            ServiceAnomalyBatchEvent event
    ) {

        kafkaTemplate.send(
                TOPIC,
                event.getServiceName(),
                event
        );

        log.info(
                "Published anomaly batch: {}",
                event.getServiceName()
        );
    }
}