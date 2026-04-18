package com.testgenieai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record VisualCompareRequest(
        UUID runId,
        @NotBlank String pageKey,
        @NotBlank String currentImageBase64,
        Double thresholdPercent
) {
}
