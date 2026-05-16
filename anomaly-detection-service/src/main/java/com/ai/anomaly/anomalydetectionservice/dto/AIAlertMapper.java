package com.ai.anomaly.anomalydetectionservice.dto;

import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;

@Component
public class AIAlertMapper {

    public AIAlertResponseDTO map(AIAnomalyAlert alert) {
        return AIAlertResponseDTO.builder().service(alert.getService()).
                severity(alert.getSeverity()).status(alert.getStatus()).
                confidence(alert.getConfidence()).mlScore(alert.getMlScore()).
                statisticalScore(alert.getStatisticalScore()).detectedAt(formatTimestamp(alert.getTimestamp())).
                rootCause(alert.getRootCause()).errorCount(alert.getErrorCount()).
                avgResponseTime(alert.getAvgResponseTime()).reasons(alert.getReasons()).build();
    }

    private String formatTimestamp(Instant timestamp) {
        long minutes = Duration.between(timestamp, Instant.now()).toMinutes();
        if (minutes < 1) {
            return "Just now";
        }
        if (minutes == 1) {
            return "1 minute ago";
        }
        if (minutes < 60) {
            return minutes + " minutes ago";
        }
        long hours = minutes / 60;
        if (hours == 1) {
            return "1 hour ago";
        }
        return hours + " hours ago";
    }
}
