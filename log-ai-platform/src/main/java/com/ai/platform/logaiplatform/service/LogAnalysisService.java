package com.ai.platform.logaiplatform.service;

import com.ai.platform.logaiplatform.dto.ServiceAnomalyBatchEvent;
import com.ai.platform.logaiplatform.entity.LogDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class LogAnalysisService {
    @Autowired
    private  OpenRouterService openRouterService;
    @Autowired
    private  LogService logService;
    @Autowired
    private  FingerprintService fingerprintService;
    @Autowired
    private  KafkaAnomalyProducer producer;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void monitorErrors() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Analysis already running");
            return;
        }
        try {
            List<LogDocument> logs = logService.fetchUnprocessedLogs();
            Map<String, List<LogDocument>> grouped = new HashMap<>();
            Map<String, List<String>> fingerprints = new HashMap<>();

            for (LogDocument log : logs) {
                String fingerprint = fingerprintService.generate(log);
                grouped.computeIfAbsent(log.getService(), k -> new ArrayList<>()).add(log);
                fingerprints.computeIfAbsent(log.getService(), k -> new ArrayList<>()).
                        add(fingerprint);
            }
            grouped.forEach((service, serviceLogs) -> {
                ServiceAnomalyBatchEvent event = ServiceAnomalyBatchEvent.builder().
                        eventId(UUID.randomUUID().toString()).serviceName(service).
                        createdAt(Instant.now()).fingerprints(fingerprints.get(service)).
                        logs(serviceLogs).build();
                producer.publish(event);

                for (LogDocument log : serviceLogs) {
                    logService.markProcessed(log.getId());}});

        } catch (Exception ex) {
            log.error("Analysis failed", ex);

        } finally {
            running.set(false);
        }
    }

    public String analyzeTrace(String traceId) throws IOException {
        List<LogDocument> logs = logService.getLogsByTraceId(traceId);
        StringBuilder builder = new StringBuilder();
        for (LogDocument log : logs) {
            builder.append("Timestamp: ").append(log.getTimestamp()).append("\n");
            builder.append("Service: ").append(log.getService()).append("\n");
            builder.append("Level: ").append(log.getLevel()).append("\n");
            builder.append("Message: ").append(log.getMessage()).append("\n");
            builder.append("Stacktrace: ").append(log.getStacktrace()).append("\n\n");
        }

        String prompt = """
            You are a senior distributed systems engineer.

            Analyze the following logs belonging
            to a single distributed transaction.

            Identify:
            1. Root Cause
            2. Failure Sequence
            3. First Failing Service
            4. Cascading Failures
            5. Severity
            6. Suggested Fix
            7. Whether retries occurred

            Logs:
            """ + builder;
        return openRouterService.askOpenRouter(prompt);
    }
    public String analyzeClusters() throws IOException {
        Map<String, Long> clusters = logService.clusterRecentErrors();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Long> entry : clusters.entrySet()) {
            builder.append("Exception: ").append(entry.getKey()).append("\n");
            builder.append("Count: ").append(entry.getValue()).append("\n\n");
        }

        String prompt = """
            You are a senior Site Reliability Engineer.

            Analyze the following exception clusters.

            Identify:
            1. Most critical cluster
            2. Most widespread issue
            3. Potential root cause
            4. Severity
            5. Suggested remediation priority

            Clusters:
            """ + builder;
        return openRouterService.askOpenRouter(prompt);
    }
    public String analyze() throws IOException {

        List<LogDocument> errors = logService.getRecentErrors();

        StringBuilder builder = new StringBuilder();

        for (LogDocument log : errors) {
            builder.append("Service: ").append(log.getService()).append("\n");
            builder.append("Level: ").append(log.getLevel()).append("\n");
            builder.append("Message: ").append(log.getMessage()).append("\n");
            builder.append("Stacktrace: ").append(log.getStacktrace()).append("\n");
            builder.append("Timestamp: ").append(log.getTimestamp()).append("\n\n");
        }

        String prompt = """
                You are a senior production support engineer.

                Analyze the following logs.

                Return:
                1. Root Cause
                2. Severity
                3. Impact
                4. Suggested Fix
                5. Affected Component
                6. Whether immediate action is required

                Logs:
                """ + builder;
        return openRouterService.askOpenRouter(prompt);
    }
}