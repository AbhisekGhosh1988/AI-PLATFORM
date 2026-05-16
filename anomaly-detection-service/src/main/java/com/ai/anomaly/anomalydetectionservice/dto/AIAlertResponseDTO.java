package com.ai.anomaly.anomalydetectionservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAlertResponseDTO {
    private String service;
    private String severity;
    private String status;
    private double confidence;
    private double mlScore;
    private double statisticalScore;
    private String detectedAt;
    private String rootCause;
    private long errorCount;
    private double avgResponseTime;
    private List<String> reasons;
}