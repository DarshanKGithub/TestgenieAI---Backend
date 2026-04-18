package com.testgenieai.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CiCdTriggerRequest(
        @NotBlank String project,
        @NotBlank String branch,
        @NotBlank String commitSha,
        @NotBlank String suiteName,
        @NotBlank String targetUrl,
        String ownerEmail,
        @Valid @NotEmpty List<TestCaseResultRequest> testCases
) {
}
