package com.testgenieai.backend.controller;

import com.testgenieai.backend.ai.GroqAiService;
import com.testgenieai.backend.dto.AiStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiStatusController {

    private final GroqAiService groqAiService;

    @GetMapping("/status")
    public AiStatusResponse getStatus() {
        return new AiStatusResponse(
                groqAiService.isConfigured(),
                groqAiService.currentModel(),
                groqAiService.currentBaseUrl(),
                groqAiService.currentMode()
        );
    }
}
