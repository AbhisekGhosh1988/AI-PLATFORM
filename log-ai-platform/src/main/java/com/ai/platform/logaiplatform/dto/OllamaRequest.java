package com.ai.platform.logaiplatform.dto;

import lombok.Data;

@Data
public class OllamaRequest {

    private String model;
    private String prompt;
    private boolean stream;
}