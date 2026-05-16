package com.ai.platform.logaiplatform.client;

import com.ai.platform.logaiplatform.dto.OllamaResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OllamaClient {

    private final WebClient.Builder builder;

    public String generate(String prompt) {

        WebClient client = builder.baseUrl("http://localhost:11434").build();

        Map<String, Object> request = new HashMap<>();
        request.put("model", "deepseek-coder:latest");
        request.put("prompt", prompt);
        request.put("stream", false);
        OllamaResponse response =
                client.post().uri("/api/generate").
                        contentType(MediaType.APPLICATION_JSON).
                        bodyValue(request).retrieve().
                        bodyToMono(OllamaResponse.class).block();

        return response.getResponse();
    }
}
