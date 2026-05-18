package com.ai.platform.logaiplatform.dto;

import lombok.Data;

import java.util.Map;

import java.util.List;

@Data
public class HeatmapResponse {

    private String service;
    private long totalErrors;
    private Map<String, Long> severities;
    private String latestTimestamp;
    private String latestImpact;
    private String latestRootCause;
    private String suggestedFix;
    private String topException;
    private List<String> traceIds;
    private String trend;
    private double anomalyScore;
    private String health;
    private List<AnomalyResponseDTO> recentAnomalies;
}