package com.ai.platform.logaiplatform.entity;


import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.Instant;

@Data
public class LogDocument {

    private String service;

    private String level;

    private String traceId;

    private String message;

    private String stacktrace;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}