package com.ai.platform.logaiplatform.service;

import com.ai.platform.logaiplatform.entity.AiAlertDocument;
import com.ai.platform.logaiplatform.entity.LogDocument;
import com.ai.platform.logaiplatform.util.AiResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LogAnalysisService {
    @Autowired
    private  OpenRouterService openRouterService;
    @Autowired
    private ObjectProvider<LogAnalysisService> logAnalysisServiceSelf;  // ✅ Lazy injection - no circular dependency
    @Autowired
    private  LogService logService;
    @Autowired
    private  AiAlertService aiAlertService;
    private  Map<String, Instant> lastAlertTimePerService = new ConcurrentHashMap<>();
    @Value("${alert.threshold}")
    private long threshold;
    @Value("${alert.cooldown-seconds}")
    private long cooldownSeconds;
    private final Set<String> processingServices =
            ConcurrentHashMap.newKeySet();

    public String analyzeService(String serviceName) throws IOException {
        List<LogDocument> logs = logService.getRecentErrorsByService(serviceName);
        StringBuilder builder = new StringBuilder();
        for (LogDocument log : logs) {
            builder.append("Service: ").append(log.getService()).append("\n");
            builder.append("Message: ").append(log.getMessage()).append("\n");
            builder.append("Stacktrace: ").append(log.getStacktrace()).append("\n\n");
        }
        log.info("Generating logs for service: {} | Log  {}", serviceName, logs);
        String prompt = """
                        You are a Log Analyst.
                        
                        Analyze ONLY the provided logs.
                        
                        STRICT RULES:
                        
                        - Return Impact
                        - Return Root Cause
                        - Return Suggested Fix but make sure you do not hallucinate
                        - Do not invent exceptions not present in logs
                        - Severity must be exactly:
                          LOW, MEDIUM, HIGH, or CRITICAL
                        - service name must be the same as the one provided  
                        Return ONLY this JSON structure:
                        { "severity": "LOW|MEDIUM|HIGH|CRITICAL", "impact": "...", "rootCause": "...", "suggestedFix": "...", "service": "..." }
                                               
                        LOGS:
                        """ + builder;
        return openRouterService.askOpenRouter(prompt);
    }

    @Scheduled(fixedRateString = "${alert.scheduler-rate}")
    public void monitorErrors() {
        try {
            log.info("Starting AI error monitoring...");
            Map<String, Long> services = logService.countErrorsByServiceLastMinute();
            if (services == null || services.isEmpty()) {
                log.info("No services with errors found");
                return;
            }
            services.forEach((serviceName, errorCount) ->
                    log.info("Service: {} | Error Count: {}", serviceName, errorCount));
            services.forEach((serviceName, errorCount) -> {
                try {
                    if (processingServices.contains(serviceName)) {
                        log.warn("Service: {} already under AI analysis", serviceName);
                        return;
                    }
                    if (errorCount < threshold) {
                        log.info("Service: {} skipped. Threshold not reached", serviceName);
                        return;
                    }
                    if (!isCooldownValid(serviceName)) {
                        log.info("Service: {} skipped due to cooldown", serviceName);
                        return;
                    }
                    processingServices.add(serviceName);
                    logErrorSpike(serviceName, errorCount);
                    logAnalysisServiceSelf.getObject().generateAlertAsync(serviceName, errorCount);

                } catch (Exception ex) {
                    processingServices.remove(serviceName);
                    log.error("Failed triggering async analysis for service: {}", serviceName, ex);
                }
            });

        } catch (Exception ex) {
            log.error("AI service alert monitoring failed", ex);
        }
    }

    /**
     * Async AI Analysis
     */
    @Async("alertAnalyzerExecutor")
    public void generateAlertAsync(String serviceName, long errorCount) {
        log.info("[ASYNC-STARTED] Analyzing service: {}", serviceName);
        try {
            String analysis = analyzeService(serviceName);
            log.info("[ASYNC-ANALYSIS] Service: {} | AI response received", serviceName);
            List<AiAlertDocument> alerts = AiResponseParser.parse(analysis);
            if (alerts == null || alerts.isEmpty()) {
                log.warn("[ASYNC-WARN] Service: {} | No alerts generated", serviceName);
                return;
            }
            for (AiAlertDocument alert : alerts) {
               if (alert.getSeverity() == null || alert.getSeverity().isBlank()) {
                    log.warn("[ASYNC-WARN] Service: {} | Severity empty", serviceName);
                    alert.setSeverity("UNKNOWN");
                }

                if (alert.getRootCause() == null || alert.getRootCause().isBlank()) {
                    alert.setRootCause("Unknown root cause");
                }

                if (alert.getImpact() == null || alert.getImpact().isBlank()) {
                    alert.setImpact("Impact not provided");
                }
                if (alert.getSuggestedFix() == null || alert.getSuggestedFix().isBlank()) {
                    alert.setSuggestedFix("No suggested fix provided");
                }
                /**
                 * Override actual service name
                 */
                alert.setService(serviceName);
                alert.setCreatedAt(Instant.now());
                log.debug("[ASYNC-ALERT] Service: {} | Severity: {} | RootCause: {}", serviceName, alert.getSeverity(), alert.getRootCause());
                /**
                 * Save alert
                 */
                aiAlertService.save(alert);
                log.info("[ASYNC-SAVED] Service: {} | Alert saved successfully", serviceName);
                /**
                 * Pretty log
                 */
                log.error("""

                        =================================
                        AI SERVICE ALERT GENERATED
                        =================================

                        Service: {}

                        Error Count: {}

                        Severity: {}

                        Root Cause:
                        {}

                        Impact:
                        {}

                        Suggested Fix:
                        {}

                        =================================
                        """, serviceName,errorCount, alert.getSeverity(), alert.getRootCause(), alert.getImpact(), alert.getSuggestedFix());
            }
            lastAlertTimePerService.put(serviceName, Instant.now());
        } catch (Exception ex) {
            log.error("[ASYNC-ERROR] Service: {} | Failed to analyze", serviceName, ex);

        } finally {
            processingServices.remove(serviceName);
            log.info("[ASYNC-END] Service: {} removed from processing", serviceName);
        }
    }


    private boolean isCooldownValid(String serviceName) {
        Instant lastAlert = lastAlertTimePerService.get(serviceName);
        return lastAlert == null || lastAlert.isBefore(Instant.now().minusSeconds(cooldownSeconds));
    }

    private void logErrorSpike(String serviceName, long errorCount) {
        log.warn("""

        =================================
        SERVICE ERROR SPIKE DETECTED
        =================================

        Service: {}
        Error Count: {}

        """, serviceName, errorCount);
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