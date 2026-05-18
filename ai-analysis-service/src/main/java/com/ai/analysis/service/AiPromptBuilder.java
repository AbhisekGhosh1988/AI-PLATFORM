package com.ai.analysis.service;

import com.ai.analysis.dto.ServiceAnomalyBatchEvent;
import com.ai.analysis.entity.LogDocument;
import org.springframework.stereotype.Service;

@Service
public class AiPromptBuilder {

    public String build(ServiceAnomalyBatchEvent event) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are a Log Analyst.

                Analyze ONLY the provided logs.
                
                STRICT RULES:

                - Return Impact
                - Return Root Cause
                - Return Suggested Fix but make sure you do not hallucinate
                - Do not invent exceptions not present in logs
                - Severity must be exactly:
                  LOW, MEDIUM, HIGH, or CRITICAL
                - service name must be the same as the one provided \s
                Return ONLY this JSON structure:
                { "severity": "LOW|MEDIUM|HIGH|CRITICAL", "impact": "...", "rootCause": "...", "suggestedFix": "...", "service": "..." }
                LOGS:
                                                       
                Return STRICT JSON ARRAY ONLY.

                """);
        prompt.append("Service: " + event.getServiceName() + "\n\n");

        for (LogDocument log : event.getLogs()) {
            prompt.append("Fingerprint: " + log.getFingerprint() + "\n");
            prompt.append("Message: " + log.getMessage() + "\n");
            prompt.append("Stacktrace: " + log.getStacktrace() + "\n\n");
        }

        return prompt.toString();
    }
}