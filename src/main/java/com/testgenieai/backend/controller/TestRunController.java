package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.CreateTestRunRequest;
import com.testgenieai.backend.dto.PagedResponse;
import com.testgenieai.backend.dto.TestRunResponse;
import com.testgenieai.backend.service.port.RunExecutionService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-runs")
@RequiredArgsConstructor
public class TestRunController {

    private final RunExecutionService runExecutionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestRunResponse execute(@Valid @RequestBody CreateTestRunRequest request) {
        return runExecutionService.executeTestRun(request);
    }

    @GetMapping
    public PagedResponse<TestRunResponse> recentRuns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String suiteName
    ) {
        return runExecutionService.getRuns(page, size, status, suiteName);
    }

    @GetMapping("/{runId}")
    public TestRunResponse getRun(@PathVariable UUID runId) {
        return runExecutionService.getRun(runId);
    }
}
