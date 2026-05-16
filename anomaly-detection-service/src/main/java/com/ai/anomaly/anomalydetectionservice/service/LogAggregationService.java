package com.ai.anomaly.anomalydetectionservice.service;

import com.ai.anomaly.anomalydetectionservice.dto.AggregatedMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogAggregationService {

    public static final String DOUBLE_QUOTES = "";
    private final RestHighLevelClient client;
    private final ObjectMapper mapper;

    public List<AggregatedMetrics> aggregateLogs(Instant lastProcessedTime) {
        try {
            log.info("======================================");
            log.info("Starting log aggregation");
            log.info("Last processed time: {}", lastProcessedTime);
            log.info("======================================");
            SearchRequest request = new SearchRequest("logs");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.rangeQuery("timestamp").
                    gte(lastProcessedTime.toString()).lte(Instant.now().toString()));
            sourceBuilder.size(10000);
            request.source(sourceBuilder);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            log.info("Total logs fetched: {}", response.getHits().getTotalHits());

            Map<String, List<JsonNode>> groupedLogs = new HashMap<>();
            for (SearchHit hit : response.getHits().getHits()) {
                try {
                    JsonNode json = mapper.readTree(hit.getSourceAsString());
                    log.info("RAW LOG: {}", json);
                    String service = json.path("service").asText("unknown");
                    groupedLogs.computeIfAbsent(service, k -> new ArrayList<>()).add(json);
                } catch (Exception e) {
                    log.error("Parsing error", e);
                }
            }
            List<AggregatedMetrics> metrics = new ArrayList<>();
            for (String service : groupedLogs.keySet()) {
                List<JsonNode> logs = groupedLogs.get(service);
                long errorCount = 0;
                long warnCount = 0;
                long criticalCount = 0;
                double totalResponseTime = 0;
                Set<String> uniqueExceptions = new HashSet<>();
                for (JsonNode logNode : logs) {
                    String level = logNode.path("level").asText(DOUBLE_QUOTES);
                    if ("ERROR".equalsIgnoreCase(level)) {
                        errorCount++;
                    }
                    if ("WARN".equalsIgnoreCase(level)) {
                        warnCount++;
                    }
                    String severity = logNode.path("severity").asText(DOUBLE_QUOTES);
                    if ("CRITICAL".equalsIgnoreCase(severity)) {
                        criticalCount++;
                    }
                    totalResponseTime += logNode.path("responseTime").asDouble(0);
                    String exception = logNode.path("exception").asText(DOUBLE_QUOTES);
                    if (exception.isBlank()) {
                        String stacktrace = logNode.path("stacktrace").asText(DOUBLE_QUOTES);
                        if (stacktrace.contains("NullPointerException")) {
                            exception = "NullPointerException";
                        }
                        else if (stacktrace.contains("SQL")) {
                            exception = "SQLException";
                        }
                        else if (stacktrace.contains("Redis")) {
                            exception = "RedisConnectionException";
                        }
                    }
                    if (!exception.isBlank()) {
                        uniqueExceptions.add(exception);
                    }
                }
                double avgResponseTime = logs.isEmpty() ? 0 : totalResponseTime / logs.size();
                AggregatedMetrics aggregated = AggregatedMetrics.builder().service(service).errorCount(errorCount).
                        warnCount(warnCount).criticalCount(criticalCount).avgResponseTime(avgResponseTime).
                        uniqueExceptionCount(uniqueExceptions.size()).build();
                metrics.add(aggregated);
                log.info("FINAL AGGREGATED METRIC: {}", aggregated);
            }
            log.info("======================================");
            log.info("Aggregation completed. Services processed: {}", metrics.size());
            log.info("======================================");
            return metrics;
        } catch (Exception e) {
            log.error("Aggregation failed", e);
            return Collections.emptyList();
        }
    }
}