package com.ai.anomaly.anomalydetectionservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLRequest {

    private int error_count;
    private int warn_count;
    private int critical_count;
    private double avg_response_time;
    private int unique_exception_count;
}