package com.ai.platform.logaiplatform.controller;


import com.ai.platform.logaiplatform.dto.LogEvent;
import com.ai.platform.logaiplatform.entity.LogDocument;
import com.ai.platform.logaiplatform.service.LogAnalysisService;
import com.ai.platform.logaiplatform.service.LogService;
import com.ai.platform.logaiplatform.service.OpenRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogAnalysisController {

    private final LogAnalysisService service;

    private final LogService logService;

    @GetMapping("/errors")
    public List<LogDocument> errors() throws IOException {
        return logService.getLatestErrors();
    }
    @GetMapping("/trace/{traceId}")
    public String analyzeTrace(@PathVariable String traceId) throws IOException {
        return service.analyzeTrace(traceId);
    }
    @GetMapping("/clusters")
    public String clusters() throws IOException {
        return service.analyzeClusters();
    }
    @PostMapping("/analyze")
    public String analyze() throws IOException {
        return service.analyze();
    }
}
