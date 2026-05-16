package com.ai.anomaly.anomalydetectionservice.service;

import com.ai.anomaly.anomalydetectionservice.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;
import com.ai.anomaly.anomalydetectionservice.dto.AIAlertMapper;
import com.ai.anomaly.anomalydetectionservice.dto.AIAlertResponseDTO;
import com.ai.anomaly.anomalydetectionservice.dto.AIAnomalyAlert;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIAlertService {
    private final RestHighLevelClient client;

    private final ObjectMapper mapper;

    private final AIAlertMapper alertMapper;

    public List<AIAlertResponseDTO> getAlerts() {
        try {
            SearchRequest request = new SearchRequest("ai-anomaly-alerts");
            SearchSourceBuilder source = new SearchSourceBuilder().size(100);
            request.source(source);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            List<AIAlertResponseDTO> alerts = new ArrayList<>();

            for (SearchHit hit : response.getHits().getHits()) {
                AIAnomalyAlert alert = mapper.readValue(hit.getSourceAsString(), AIAnomalyAlert.class);
                alerts.add(alertMapper.map(alert));
            }
            return alerts;

        } catch (Exception e) {
            log.error("Failed to fetch alerts", e);
            return List.of();
        }
    }

    public AIAnomalyAlert buildAlert(ServiceMetric metric, ServiceAnomaly statistical, MLResponse ml) {
        String severity = calculateSeverity(statistical, ml);
        return AIAnomalyAlert.builder().service(metric.getService()).timestamp(metric.getTimestamp()).
                statisticalAnomaly(statistical.isAnomaly()).mlAnomaly(ml.isAnomaly()).
                statisticalScore(statistical.getAnomalyScore()).mlScore(ml.getScore()).
                severity(severity).status("CRITICAL".equals(severity) ? "OPEN" : "MONITORING").
                confidence(Math.min((Math.abs(ml.getScore()) + Math.abs(statistical.getAnomalyScore())) * 10, 100)).
                errorCount(metric.getErrorCount()).avgResponseTime(metric.getAvgResponseTime()).
                rootCause(extractRootCause(ml.getReasons())).reasons(ml.getReasons()).build();
    }

    private String extractRootCause(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return "Unknown";
        }
        return reasons.get(0);
    }

    private String calculateSeverity(ServiceAnomaly statistical, MLResponse ml) {
        if (statistical.isAnomaly() && ml.isAnomaly()) {
            return "CRITICAL";
        }
        if (statistical.isAnomaly() || ml.isAnomaly()) {
            return "HIGH";
        }
        return "NORMAL";
    }
}