package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.FailureAnalysisRequest;
import com.testgenieai.backend.dto.FailureAnalysisResponse;
import com.testgenieai.backend.service.port.AiAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping("/failure-analysis")
    public FailureAnalysisResponse analyzeFailure(@Valid @RequestBody FailureAnalysisRequest request) {
        return aiAnalysisService.analyzeFailure(request);
    }
}
