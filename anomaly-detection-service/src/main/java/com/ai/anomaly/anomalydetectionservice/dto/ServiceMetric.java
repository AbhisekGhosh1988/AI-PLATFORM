package com.ai.anomaly.anomalydetectionservice.dto;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceMetric {

    private String service;
    private Instant timestamp;
    private int errorCount;
    private int warnCount;
    private int criticalCount;
    private double avgResponseTime;
    private int uniqueExceptionCount;
}