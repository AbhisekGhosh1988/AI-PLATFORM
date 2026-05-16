package com.ai.platform.logaiplatform.controller;

import com.ai.platform.logaiplatform.dto.ServiceAnomaly;

import com.ai.platform.logaiplatform.service.AnomalyDetectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyDetectionService service;

    @GetMapping
    public List<ServiceAnomaly> anomalies(@RequestParam(defaultValue = "5") int minutes)
            throws IOException {
        return service.detectAnomalies(minutes);
    }
}