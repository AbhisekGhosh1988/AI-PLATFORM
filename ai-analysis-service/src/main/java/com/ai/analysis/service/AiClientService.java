package com.ai.analysis.service;


import com.ai.analysis.entity.AiAlertDocument;
import com.ai.analysis.util.AiResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AiClientService {
    @Autowired
    private  OpenRouterService openRouterService;
    public List<AiAlertDocument> analyze(String prompt) {

        String response = openRouterService.askOpenRouter(prompt);
        List<AiAlertDocument> alerts = AiResponseParser.parse(response);
        return alerts;
    }
}
