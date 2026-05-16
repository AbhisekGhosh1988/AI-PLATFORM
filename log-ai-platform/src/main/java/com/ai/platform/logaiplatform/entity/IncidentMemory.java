package com.ai.platform.logaiplatform.entity;

import lombok.Data;

import java.time.Instant;

import java.util.List;

@Data
public class IncidentMemory {

    private String incidentText;

    private String resolution;

    private String service;

    private Instant timestamp;

    private List<Double> embedding;
}