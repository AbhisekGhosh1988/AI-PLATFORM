package com.ai.platform.logaiplatform.service;

import com.ai.platform.logaiplatform.dto.ServiceAnomaly;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final LogService logService;

    public List<ServiceAnomaly> detectAnomalies(int minutes) throws IOException {

        Map<String, Long> services = logService.countErrorsByServiceByTimeRange(minutes);
        List<ServiceAnomaly> anomalies = new ArrayList<>();
        for (String service : services.keySet()) {
            double average = logService.averageErrorsPerMinute(service);
            long current = logService.currentErrorsLastMinute(service);
            double deviation = current - average;
            /*
             * Simple anomaly rule
             */
            boolean anomaly = current > (average * 3);
            anomalies.add(new ServiceAnomaly(service, average, current, deviation, anomaly));
        }

        return anomalies;
    }
}
