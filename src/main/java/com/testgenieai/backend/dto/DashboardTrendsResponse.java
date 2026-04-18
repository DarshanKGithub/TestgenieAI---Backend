package com.testgenieai.backend.dto;

import java.util.List;

public record DashboardTrendsResponse(
        List<PassFailTrendPointResponse> passFailTrend,
        List<DurationTrendPointResponse> durationTrend
) {
}
