package com.ai.platform.logaiplatform.client;

import com.ai.platform.logaiplatform.dto.EmbeddingResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OllamaEmbeddingClient {

    private final WebClient.Builder builder;

    public List<Double> embed(
            String text) {

        WebClient client =
                builder.baseUrl(
                        "http://localhost:11434"
                ).build();

        Map<String, Object> request =
                new HashMap<>();

        request.put(
                "model",
                "nomic-embed-text"
        );

        request.put(
                "prompt",
                text
        );

        EmbeddingResponse response =
                client.post()
                        .uri("/api/embeddings")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(
                                EmbeddingResponse.class
                        )
                        .block();

        return response.getEmbedding();
    }
}