package com.testgenieai.backend.dto;

import java.time.LocalDateTime;

public record VisualBaselineResponse(
        String pageKey,
        String baselineHash,
        LocalDateTime updatedAt
) {
}
