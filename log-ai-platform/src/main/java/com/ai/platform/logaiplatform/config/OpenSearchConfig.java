package com.ai.platform.logaiplatform.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.hc.core5.http.HttpHost;

import org.opensearch.client.RestClient;

import org.opensearch.client.json.jackson.JacksonJsonpMapper;

import org.opensearch.client.opensearch.OpenSearchClient;

import org.opensearch.client.transport.OpenSearchTransport;

import org.opensearch.client.transport.rest_client.RestClientTransport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    @Bean
    public OpenSearchClient openSearchClient() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
        OpenSearchTransport transport = new RestClientTransport
                (restClient, new JacksonJsonpMapper(objectMapper));
        return new OpenSearchClient(transport);
    }
}