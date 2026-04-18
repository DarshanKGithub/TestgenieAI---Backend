package com.testgenieai.backend.dto;

public record DashboardSummaryResponse(
        long totalRuns,
        long successfulRuns,
        long failedRuns,
        double passRate,
        long avgDurationMs
) {
}
