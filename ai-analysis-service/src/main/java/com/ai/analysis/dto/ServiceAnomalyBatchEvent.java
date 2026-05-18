package com.ai.analysis.dto;

import com.ai.analysis.entity.LogDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAnomalyBatchEvent {

    private String eventId;

    private String serviceName;

    private Instant createdAt;

    private List<String> fingerprints;

    private List<LogDocument> logs;
}