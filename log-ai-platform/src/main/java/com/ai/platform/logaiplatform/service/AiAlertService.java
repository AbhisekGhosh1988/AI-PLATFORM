package com.ai.platform.logaiplatform.service;

import com.ai.platform.logaiplatform.client.AnomalyClient;
import com.ai.platform.logaiplatform.dto.AnomalyResponseDTO;
import com.ai.platform.logaiplatform.dto.HeatmapResponse;
import com.ai.platform.logaiplatform.entity.AiAlertDocument;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiAlertService {

    private final OpenSearchClient client;
    private final AnomalyClient anomalyClient;
    public void save(AiAlertDocument alert) throws IOException {

        // ✅ Ensure severity is set to a valid value (not null or empty)
        if (alert.getSeverity() == null || alert.getSeverity().isBlank()) {
            alert.setSeverity("UNKNOWN");
        }
        // ✅ Extract only the first line of severity if it's multi-line
        alert.setSeverity(alert.getSeverity().split("\n")[0].trim());

        IndexResponse response = client.index(i ->
                i.index("ai-alerts").id(UUID.randomUUID().toString()).document(alert));
    }

    public Map<String, Map<String, Long>>
    getSeverityHeatmap(String duration)
            throws IOException {

        SearchResponse<AiAlertDocument> response = client.search(s -> s.index("ai-alerts").size(0).
                query(q -> q.range(r -> r.field("createdAt").gte(JsonData.of("now-" + duration))))
                .aggregations("services", a -> a.terms(t -> t.field("service.keyword").size(100))
                .aggregations("severity", sub -> sub.terms(tt -> tt.field("severity.keyword").
                        size(100)))), AiAlertDocument.class);

        Map<String, Map<String, Long>> result = new HashMap<>();

        if (response.aggregations() == null || response.aggregations().get("services") == null) {
            return result;
        }
        StringTermsAggregate services = response.aggregations().get("services").sterms();
        for (StringTermsBucket serviceBucket : services.buckets().array()) {
            String serviceName = serviceBucket.key();
            Map<String, Long> severities = new HashMap<>();
            if (serviceBucket.aggregations() != null
                    && serviceBucket.aggregations().get("severity") != null) {
                StringTermsAggregate severityAgg = serviceBucket.aggregations().get("severity").sterms();
                for (StringTermsBucket severityBucket :
                        severityAgg.buckets().array()) {
                    String severityKey = severityBucket.key().isEmpty() ? "UNKNOWN" : severityBucket.key();
                    severities.put(severityKey, severityBucket.docCount());
                }
            }
            result.put(serviceName, severities);
        }
        return result;
    }
    public List<AiAlertDocument> getAllAlerts() throws IOException {
        SearchResponse<AiAlertDocument> response = client.search(s ->
                        s.index("ai-alerts").size(10000),
                AiAlertDocument.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .toList();
    }
    private static final Pattern VALID_DURATION =
            Pattern.compile("^\\d+[smhd]$");

    public List<HeatmapResponse> getDetailedHeatmap(String duration)
            throws IOException {

        if (!VALID_DURATION.matcher(duration).matches()) {
            throw new IllegalArgumentException(
                    "Invalid duration format. Examples: 5m, 1h, 7d"
            );
        }

        SearchResponse<AiAlertDocument> response =
                client.search(s -> s
                                .index("ai-alerts")
                                .size(1000)
                                .query(q -> q
                                        .range(r -> r
                                                .field("createdAt")
                                                .gte(JsonData.of("now-" + duration))
                                                .lte(JsonData.of("now"))
                                        )
                                ),
                        AiAlertDocument.class);

        Map<String, HeatmapResponse> map = new HashMap<>();

        List<AnomalyResponseDTO> alerts = anomalyClient.getAlerts();

        for (Hit<AiAlertDocument> hit : response.hits().hits()) {

            AiAlertDocument alert = hit.source();

            if (alert == null) {
                continue;
            }

            String service = alert.getService();

            HeatmapResponse dto = map.computeIfAbsent(service, s -> {
                HeatmapResponse h = new HeatmapResponse();
                h.setService(service);
                h.setSeverities(new HashMap<>());
                h.setTraceIds(new ArrayList<>());
                return h;
            });

            dto.setTotalErrors(dto.getTotalErrors() + 1);

            String severity =
                    alert.getSeverity() == null
                            ? "UNKNOWN"
                            : alert.getSeverity();

            dto.getSeverities().merge(severity, 1L, Long::sum);

            if (alert.getCreatedAt() != null) {
                dto.setLatestTimestamp(alert.getCreatedAt().toString());
            }

            dto.setLatestImpact(alert.getImpact());
            dto.setLatestRootCause(alert.getRootCause());
            dto.setSuggestedFix(alert.getSuggestedFix());

            dto.setTopException(
                    extractTopException(alert.getRootCause())
            );

            dto.setTrend(calculateTrend(dto.getTotalErrors()));

            dto.setHealth(
                    calculateHealth(severity, dto.getTotalErrors())
            );

            dto.setRecentAnomalies(alerts);
        }

        return new ArrayList<>(map.values());
    }
    private String extractTopException(String rootCause) {

        if (rootCause == null) {
            return "UNKNOWN";
        }

        return rootCause.contains("Redis") ? "RedisConnectionException" :
                rootCause.contains("SQL") ? "SQLTimeoutException" :
                rootCause.contains("NullPointer") ? "NullPointerException" : "UNKNOWN";
    }

    private String calculateTrend(long totalErrors) {

        return totalErrors >= 20 ? "RISING_FAST" : totalErrors >= 10 ?
        "RISING" : totalErrors >= 5 ? "STABLE" : "LOW";
    }

    private String calculateHealth(String severity, long totalErrors) {
        return "CRITICAL".equalsIgnoreCase(severity) || totalErrors >= 20 ? "DOWN" :
                "HIGH".equalsIgnoreCase(severity) || totalErrors >= 10 ? "UNSTABLE" :
                "HEALTHY";
    }
}
