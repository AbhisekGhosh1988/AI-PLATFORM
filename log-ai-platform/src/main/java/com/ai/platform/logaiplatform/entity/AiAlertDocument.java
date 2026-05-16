package com.ai.platform.logaiplatform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.Data;

import java.time.Instant;

@Data
public class AiAlertDocument {

    private String severity;
    private String impact;
    private String rootCause;
    private String suggestedFix;
    private String service;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;
}