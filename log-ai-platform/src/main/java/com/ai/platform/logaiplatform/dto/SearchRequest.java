package com.ai.platform.logaiplatform.dto;

import lombok.Data;

@Data
public class SearchRequest {

    private String service;

    private String level;

    private String keyword;
}
