package com.ai.platform.logaiplatform.dto;

import lombok.Data;

@Data
public class LogEvent {

    private String timestamp;
    private String level;
    private String service;
    private String traceId;
    private String message;
    private String stacktrace;
}
