package com.ai.platform.logaiplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class OpenRouterChatRequest {
    private String model;
    private List<OpenRouterMessage> messages;
    private OpenRouterReasoning reasoning;
}
