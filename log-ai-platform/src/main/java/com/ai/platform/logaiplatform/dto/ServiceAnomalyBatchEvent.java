package com.ai.platform.logaiplatform.dto;

import com.ai.platform.logaiplatform.entity.LogDocument;
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