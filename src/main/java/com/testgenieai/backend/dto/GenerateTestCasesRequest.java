package com.testgenieai.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateTestCasesRequest(
        @NotBlank String targetUrl,
        @NotBlank String userFlow
) {
}
