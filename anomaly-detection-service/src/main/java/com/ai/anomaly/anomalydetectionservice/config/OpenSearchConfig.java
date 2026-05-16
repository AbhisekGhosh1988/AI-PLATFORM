package com.ai.anomaly.anomalydetectionservice.config;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenSearchConfig {

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    }
}