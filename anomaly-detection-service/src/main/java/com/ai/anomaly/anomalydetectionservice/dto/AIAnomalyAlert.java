package com.ai.anomaly.anomalydetectionservice.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnomalyAlert {

    private String service;
    private Instant timestamp;
    private boolean statisticalAnomaly;
    private boolean mlAnomaly;
    private double statisticalScore;
    private double mlScore;
    private String severity;
    private String status;
    private double confidence;
    private String rootCause;
    private long errorCount;
    private double avgResponseTime;
    private List<String> reasons;
}