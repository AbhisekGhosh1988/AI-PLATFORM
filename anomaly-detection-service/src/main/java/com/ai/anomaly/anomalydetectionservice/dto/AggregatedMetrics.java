package com.ai.anomaly.anomalydetectionservice.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedMetrics {
    private String service;
    private long errorCount;
    private long warnCount;
    private long criticalCount;
    private double avgResponseTime;
    private long uniqueExceptionCount;
}