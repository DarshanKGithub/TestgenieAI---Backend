package com.testgenieai.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateTestRunRequest(
        @NotBlank String suiteName,
        @NotBlank String targetUrl,
        @Valid @NotEmpty List<TestCaseResultRequest> testCases
) {
}
