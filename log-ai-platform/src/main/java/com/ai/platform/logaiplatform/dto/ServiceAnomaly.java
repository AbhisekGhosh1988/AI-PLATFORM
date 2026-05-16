package com.ai.platform.logaiplatform.dto;

import lombok.AllArgsConstructor;

import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceAnomaly {

    private String service;

    private Double averageErrors;

    private Long currentErrors;

    private Double deviation;

    private Boolean anomalyDetected;
}