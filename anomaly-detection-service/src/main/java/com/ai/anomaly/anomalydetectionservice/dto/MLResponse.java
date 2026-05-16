package com.ai.anomaly.anomalydetectionservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLResponse {

    private boolean anomaly;
    private double score;
    private List<String> reasons;
}