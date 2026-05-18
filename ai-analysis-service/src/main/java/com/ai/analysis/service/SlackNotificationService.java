package com.ai.analysis.service;


import com.ai.analysis.dto.SlackMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final WebClient webClient = WebClient.builder().build();

    public void sendNotification(String message) {

        SlackMessage payload = new SlackMessage(message);

        try {

            String response = webClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Slack notification sent: {}", response);

        } catch (Exception e) {
            log.error("Failed to send Slack notification", e);
        }
    }
}