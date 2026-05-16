package com.ai.anomaly.anomalydetectionservice.service;

import com.ai.anomaly.anomalydetectionservice.dto.ServiceAnomaly;
import com.ai.anomaly.anomalydetectionservice.dto.ServiceMetric;
import com.ai.anomaly.anomalydetectionservice.util.StatisticsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticalAnomalyService {

    private final HistoricalMetricsService historicalService;

    public ServiceAnomaly analyze(ServiceMetric metric) {

        List<Double> history = historicalService.getHistoricalErrorCounts(metric.getService());

        double average = StatisticsUtil.average(history);

        double stdDev = StatisticsUtil.standardDeviation(history, average);
        double current = metric.getErrorCount();
        double threshold = average + (2 * stdDev);
        boolean anomaly = current > threshold && current >= 20;
        double score = stdDev == 0 ? 0 : (current - average) / stdDev;
        String reason;
        if (anomaly) {
            reason = "Error spike detected. Current errors=" + current +
                    ", expected threshold=" + threshold;

        } else {
            reason = "Normal behavior";
        }

        return ServiceAnomaly.builder().service(metric.getService()).
                timestamp(Instant.now()).average(average).standardDeviation(stdDev).
                currentValue(current).anomalyScore(score).anomaly(anomaly).
                reason(reason).build();
    }
}
