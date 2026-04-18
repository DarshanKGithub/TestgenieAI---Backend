package com.testgenieai.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record FailureAnalysisRequest(
        String suiteName,
        String testName,
        @NotBlank String errorLog
) {
}
