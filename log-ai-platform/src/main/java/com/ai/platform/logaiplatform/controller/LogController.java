package com.ai.platform.logaiplatform.controller;




import com.ai.platform.logaiplatform.dto.SearchRequest;
import com.ai.platform.logaiplatform.entity.LogDocument;
import com.ai.platform.logaiplatform.service.LogService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService service;

    @PostMapping
    public String save(@RequestBody LogDocument log) throws IOException {
        return service.save(log);
    }

    @PostMapping("/search")
    public List<LogDocument> search(@RequestBody SearchRequest request) throws IOException {
        return service.search(request.getService(), request.getLevel(), request.getKeyword());
    }
}