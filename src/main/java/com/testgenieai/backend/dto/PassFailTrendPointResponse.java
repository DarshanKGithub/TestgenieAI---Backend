package com.testgenieai.backend.dto;

import java.time.LocalDate;

public record PassFailTrendPointResponse(
        LocalDate date,
        long passedRuns,
        long failedRuns
) {
}
