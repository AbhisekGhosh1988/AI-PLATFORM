package com.ai.analysis.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogDocument {

    private String id;

    private String service;

    private String level;

    private String traceId;

    private String message;

    private String stacktrace;

    private String fingerprint;

    private Boolean processed = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}