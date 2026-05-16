package com.ai.anomaly.anomalydetectionservice.repository;


import com.ai.anomaly.anomalydetectionservice.dto.ServiceMetric;
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
public class ServiceMetricRepository {

    private final RestHighLevelClient client;

    private final ObjectMapper objectMapper;

    public void save(ServiceMetric metric) {

        try {
            String json = objectMapper.writeValueAsString(metric);
            IndexRequest request = new IndexRequest("service-metrics")
                    .source(json, XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("Failed to save metric", e);
        }
    }
}