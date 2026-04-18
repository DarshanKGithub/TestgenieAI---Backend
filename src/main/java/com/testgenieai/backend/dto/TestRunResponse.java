package com.testgenieai.backend.dto;

import com.testgenieai.backend.domain.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TestRunResponse(
        UUID id,
        String suiteName,
        String targetUrl,
        ExecutionStatus status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        int totalTests,
        int passedTests,
        int failedTests,
        Long durationMs,
        List<TestCaseResultResponse> testCases
) {
}
