package com.ai.anomaly.anomalydetectionservice.client;

import com.ai.anomaly.anomalydetectionservice.dto.MLRequest;
import com.ai.anomaly.anomalydetectionservice.dto.MLResponse;
import com.ai.anomaly.anomalydetectionservice.dto.ServiceMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLAnomalyClient {

    private final WebClient mlWebClient;

    public MLResponse predict(ServiceMetric metric) {
        try {

            MLRequest request = MLRequest.builder().error_count(metric.getErrorCount()).
                    warn_count(metric.getWarnCount()).critical_count(metric.getCriticalCount()).
                    avg_response_time(metric.getAvgResponseTime()).unique_exception_count(metric.getUniqueExceptionCount()).build();

            return mlWebClient.post().uri("/predict").bodyValue(request).retrieve().bodyToMono(MLResponse.class).block();

        } catch (Exception e) {
            log.error("ML prediction failed", e);
            return MLResponse.builder().anomaly(false).score(0).build();
        }
    }
}