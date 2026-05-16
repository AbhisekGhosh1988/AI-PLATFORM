package com.ai.anomaly.anomalydetectionservice.service;

import com.ai.anomaly.anomalydetectionservice.client.MLAnomalyClient;
import com.ai.anomaly.anomalydetectionservice.dto.*;
import com.ai.anomaly.anomalydetectionservice.repository.AIAnomalyAlertRepository;
import com.ai.anomaly.anomalydetectionservice.repository.ServiceAnomalyRepository;
import com.ai.anomaly.anomalydetectionservice.repository.ServiceMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsScheduler {

    private final LogAggregationService aggregationService;

    private final ServiceMetricRepository repository;

    private final StatisticalAnomalyService anomalyService;

    private final MLAnomalyClient mlClient;

    private final AIAlertService aiAlertService;

    private final AIAnomalyAlertRepository alertRepository;

    private final ServiceAnomalyRepository anomalyRepository;

    /*
     * Prevent duplicate processing
     */
    private Instant lastProcessedTime =
            Instant.now().minusSeconds(60);

    @Scheduled(fixedRate = 30000)
    public void generateMetrics() {

        log.info("======================================");
        log.info("Starting metrics aggregation...");
        log.info("Last processed time: {}", lastProcessedTime);
        log.info("======================================");
        List<AggregatedMetrics> metrics = aggregationService.aggregateLogs(lastProcessedTime);
        lastProcessedTime = Instant.now();
        log.info("Aggregated metrics count: {}", metrics.size());
        log.info("Aggregated metrics data: {}", metrics);
        if (metrics.isEmpty()) {
            log.warn("No metrics found.");
            return;
        }

        for (AggregatedMetrics metric : metrics) {
            try {
                log.info("Processing service: {}", metric.getService());
                ServiceMetric serviceMetric = ServiceMetric.builder().service(metric.getService()).
                        timestamp(Instant.now()).errorCount((int) metric.getErrorCount()).
                        warnCount((int) metric.getWarnCount()).criticalCount((int) metric.getCriticalCount()).
                        avgResponseTime(metric.getAvgResponseTime()).
                        uniqueExceptionCount((int) metric.getUniqueExceptionCount()).build();
                log.info("Generated ServiceMetric: {}", serviceMetric);
                repository.save(serviceMetric);
                log.info("ServiceMetric saved successfully");
                ServiceAnomaly anomaly = anomalyService.analyze(serviceMetric);
                log.info("Generated Statistical Anomaly: {}", anomaly);
                anomalyRepository.save(anomaly);
                log.info("Statistical anomaly saved successfully");
                MLResponse mlResponse = mlClient.predict(serviceMetric);
                log.info("ML Response: {}", mlResponse);
                AIAnomalyAlert aiAlert = aiAlertService.buildAlert(serviceMetric, anomaly, mlResponse);
                log.info("Generated AI Alert: {}", aiAlert);
                alertRepository.save(aiAlert);
                log.info("AI Alert saved successfully");
                if ("CRITICAL".equalsIgnoreCase(aiAlert.getSeverity())) {
                    log.error("CRITICAL AI ANOMALY DETECTED: {}", aiAlert.getService());
                }
                if (anomaly.isAnomaly()) {
                    log.error("STATISTICAL ANOMALY DETECTED: {} score={}", anomaly.getService(), anomaly.getAnomalyScore());
                }
                log.info("Completed processing for service: {}", metric.getService());

            } catch (Exception e) {
                log.error("FAILED PROCESSING SERVICE: {}", metric.getService(), e);
            }
        }

        log.info("======================================");
        log.info("Metrics aggregation completed");
        log.info("======================================");
    }
}