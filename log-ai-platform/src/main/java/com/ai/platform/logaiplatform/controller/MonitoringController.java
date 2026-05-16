package com.ai.platform.logaiplatform.controller;

import com.ai.platform.logaiplatform.dto.HeatmapResponse;
import com.ai.platform.logaiplatform.entity.AiAlertDocument;
import com.ai.platform.logaiplatform.service.AiAlertService;
import com.ai.platform.logaiplatform.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class MonitoringController {

    private final LogService service;
    private final AiAlertService aiAlertService;

    @GetMapping("/top-errors")
    public Map<String, Long> getTopErrors(@RequestParam(defaultValue = "5") int minutes)
            throws IOException {
        return service.getTopErrors(minutes);
    }

    @GetMapping("/heatmap/details")
    public List<HeatmapResponse>
    getDetailedHeatmap(@RequestParam(defaultValue = "10m") String duration)
            throws IOException {
        return aiAlertService.getDetailedHeatmap(duration);
    }

    @GetMapping("/alerts/all")
    public List<AiAlertDocument> getAllAlerts() throws IOException {
        return aiAlertService.getAllAlerts();
    }
}
