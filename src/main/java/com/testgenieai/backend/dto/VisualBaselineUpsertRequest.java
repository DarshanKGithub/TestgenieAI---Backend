package com.testgenieai.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record VisualBaselineUpsertRequest(
        @NotBlank String pageKey,
        @NotBlank String baselineImageBase64
) {
}
