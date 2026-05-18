package com.ai.analysis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenRouterMessage {
    private String role;
    private String content;
}
