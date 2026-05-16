package com.ai.anomaly.anomalydetectionservice.controller;

import com.ai.anomaly.anomalydetectionservice.dto.AIAlertResponseDTO;
import com.ai.anomaly.anomalydetectionservice.service.AIAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AnomalyController {
    private final AIAlertService alertService;

    @GetMapping
    public List<AIAlertResponseDTO> getAlerts() {
        return alertService.getAlerts();
    }
}
