package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.GenerateTestCasesRequest;
import com.testgenieai.backend.dto.GenerateTestCasesResponse;
import com.testgenieai.backend.service.port.TestCaseGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-cases")
@RequiredArgsConstructor
public class TestCaseGenerationController {

    private final TestCaseGenerationService testCaseGenerationService;

    @PostMapping("/generate")
    public GenerateTestCasesResponse generate(@Valid @RequestBody GenerateTestCasesRequest request) {
        return testCaseGenerationService.generateTestCases(request);
    }
}
