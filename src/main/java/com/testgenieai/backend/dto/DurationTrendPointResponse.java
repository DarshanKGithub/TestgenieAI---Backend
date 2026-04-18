package com.testgenieai.backend.dto;

import java.time.LocalDate;

public record DurationTrendPointResponse(
        LocalDate date,
        long averageDurationMs
) {
}
