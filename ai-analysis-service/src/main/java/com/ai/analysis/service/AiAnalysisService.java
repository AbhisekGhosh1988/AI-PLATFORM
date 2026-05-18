package com.ai.analysis.service;

import com.ai.analysis.dto.JiraIssueRequest;
import com.ai.analysis.dto.ServiceAnomalyBatchEvent;
import com.ai.analysis.entity.AiAlertDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final AiPromptBuilder promptBuilder;
    private final AiClientService aiClientService;
    private final AiAlertStorageService storageService;
    private final SlackNotificationService slackService;
    private final JiraTicketService jiraService;

    public void analyze(ServiceAnomalyBatchEvent event) {
        try {
            // CALLER IS HERE
            String prompt = promptBuilder.build(event);
            log.info("Generated AI prompt");
            // AI MODEL CALL
            List<AiAlertDocument> aiResponse = aiClientService.analyze(prompt);
            List<String> alerts = aiResponse.stream().map(aiAlertDocument -> {
                // 1. Extract values from the document
                String severity = aiAlertDocument.getSeverity();
                String service = aiAlertDocument.getService();

                // 2. Format the individual alert message
                return String.format("""
                *🚨 AI Alert Detected*
                                
                *Service*: %s
                *Error Count*: %d
                *Severity*: %s
                *Root Cause*: %s
                *Impact*: %s
                *Suggested fix* : %s
                """, service, event.getLogs().size(),
                        severity, aiAlertDocument.getRootCause(),
                        aiAlertDocument.getImpact(), aiAlertDocument.getSuggestedFix());
            }).toList();
            alerts.forEach(msg -> slackService.sendNotification(msg));
            aiResponse.forEach(s -> jiraService.createTicket(s.getService(),
                    s.getSeverity(), s.getRootCause(), event.getLogs().size()));
            log.info("AI Response: {}", aiResponse);
            for (AiAlertDocument item : aiResponse) {
                item.setCreatedAt(Instant.now());
                storageService.save(item);
            }
            log.info("AI analysis completed for service: {}", event.getServiceName());

        } catch (Exception ex) {
            log.error("AI analysis failed", ex);
        }

    }
}
