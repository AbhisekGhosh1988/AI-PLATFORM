package com.ai.anomaly.anomalydetectionservice.repository;

import com.ai.anomaly.anomalydetectionservice.dto.AIAnomalyAlert;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AIAnomalyAlertRepository {

    private final RestHighLevelClient client;

    private final ObjectMapper mapper;

    public void save(AIAnomalyAlert alert) {

        try {
            String json = mapper.writeValueAsString(alert);
            log.info("Saving AI alert JSON: {}", json);
            IndexRequest request = new IndexRequest("ai-anomaly-alerts").source(json, XContentType.JSON);

            client.index(request, RequestOptions.DEFAULT);
            log.info("AI alert saved into OpenSearch");

        } catch (Exception e) {
            log.error("FAILED TO SAVE AI ALERT", e);
        }
    }
}