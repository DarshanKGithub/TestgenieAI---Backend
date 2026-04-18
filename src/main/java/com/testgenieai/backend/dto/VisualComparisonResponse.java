package com.testgenieai.backend.dto;

import com.testgenieai.backend.domain.VisualDiffStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record VisualComparisonResponse(
        UUID id,
        String pageKey,
        String baselineImagePath,
        String currentImagePath,
        String diffImagePath,
        double diffPercent,
        VisualDiffStatus status,
        LocalDateTime createdAt
) {
}
