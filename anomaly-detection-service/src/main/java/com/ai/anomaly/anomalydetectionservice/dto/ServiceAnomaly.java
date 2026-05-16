package com.ai.anomaly.anomalydetectionservice.dto;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAnomaly {

    private String service;
    private Instant timestamp;
    private double average;
    private double standardDeviation;
    private double currentValue;
    private double anomalyScore;
    private boolean anomaly;
    private String reason;
}