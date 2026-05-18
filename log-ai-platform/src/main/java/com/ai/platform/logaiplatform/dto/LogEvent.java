package com.ai.platform.logaiplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogEvent {

    private String timestamp;
    private String level;
    private String service;
    private String traceId;
    private String message;
    private String stacktrace;
}
