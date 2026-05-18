package com.ai.analysis.service;


import com.ai.analysis.dto.JiraIssueRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraTicketService {

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.email}")
    private String email;

    @Value("${jira.api-token}")
    private String apiToken;

    @Value("${jira.project-key}")
    private String projectKey;

    private final WebClient webClient = WebClient.builder().build();

    public String createTicket(String serviceName, String severity,
                               String rootCause, long errorCount) {
        JiraIssueRequest request = buildRequest(serviceName, severity, rootCause, errorCount);
        String auth = email + ":" + apiToken;
        try {
            String encodedAuth = Base64.getEncoder().
                    encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            String response = webClient.post().
                    uri(jiraUrl + "/rest/api/2/issue").
                    header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth).
                    contentType(MediaType.APPLICATION_JSON).bodyValue(request).
                    retrieve().onStatus(HttpStatusCode::isError,
                    clientResponse -> clientResponse.bodyToMono(String.class).
                            flatMap(errorBody -> {log.error("Jira Error Response: {}",
                                    errorBody);return Mono.error(new RuntimeException(errorBody));
                            })).bodyToMono(String.class).block();
            log.info("Jira ticket created: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to create Jira ticket", e);
            return null;
        }
    }

    private JiraIssueRequest buildRequest(String serviceName, String severity,
                                          String rootCause, long errorCount) {
        JiraIssueRequest request = new JiraIssueRequest();
        JiraIssueRequest.Fields fields = new JiraIssueRequest.Fields();
        JiraIssueRequest.Project project = new JiraIssueRequest.Project();
        JiraIssueRequest.IssueType issueType = new JiraIssueRequest.IssueType();
        project.setKey(projectKey);
        issueType.setName("Task");
        fields.setProject(project);
        fields.setIssuetype(issueType);
        fields.setSummary("[AI ALERT] " + serviceName + " - " + severity);
        fields.setDescription("""
                AI Alert Detected
                
                Service: %s
                Severity: %s
                Error Count: %s
                
                Root Cause:
                %s
                """.formatted(serviceName, severity, errorCount, rootCause));
        request.setFields(fields);
        return request;
    }
}