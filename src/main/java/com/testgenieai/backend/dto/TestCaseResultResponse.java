package com.testgenieai.backend.dto;

import com.testgenieai.backend.domain.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TestCaseResultResponse(
        UUID id,
        String testName,
        ExecutionStatus status,
        String errorMessage,
        long durationMs,
        LocalDateTime executedAt
) {
}
