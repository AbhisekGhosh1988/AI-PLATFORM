package com.ai.anomaly.anomalydetectionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricalMetricsService {

    private final RestHighLevelClient client;
    private final ObjectMapper mapper;

    public List<Double> getHistoricalErrorCounts(String service) {

        try {
            SearchRequest request = new SearchRequest("service-metrics");
            BoolQueryBuilder query = QueryBuilders.boolQuery().
                    must(QueryBuilders.termQuery("service.keyword", service)).
                    must(QueryBuilders.rangeQuery("timestamp").gte("now-1h").lte("now"));

            SearchSourceBuilder source = new SearchSourceBuilder().query(query).size(1000);
            request.source(source);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            List<Double> values = new ArrayList<>();

            Arrays.stream(response.getHits().getHits())
                    .forEach(hit -> {
                        try {
                            JsonNode json = mapper.readTree(hit.getSourceAsString());
                            values.add(json.path("errorCount").asDouble(0));
                        } catch (Exception ignored) {
                        }
                    });
            return values;

        } catch (Exception e) {
            return List.of();
        }
    }
}