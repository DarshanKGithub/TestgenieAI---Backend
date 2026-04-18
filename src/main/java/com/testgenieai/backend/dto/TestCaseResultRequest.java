package com.testgenieai.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TestCaseResultRequest(
        @NotBlank String testName,
        @NotBlank String path,
        String expectedText
) {
}
