package com.ai.anomaly.anomalydetectionservice.controller;
import com.ai.anomaly.anomalydetectionservice.dto.ServiceMetric;
import com.ai.anomaly.anomalydetectionservice.repository.ServiceMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricTestController {

    private final ServiceMetricRepository repository;

    @PostMapping("/test")
    public String test() {

        ServiceMetric metric = ServiceMetric.builder()
                .service("payment-service").timestamp(Instant.now()).errorCount(120).warnCount(40).
                criticalCount(10).avgResponseTime(2400).uniqueExceptionCount(5).build();
        repository.save(metric);
        return "Metric inserted";
    }
}