package com.testgenieai.backend.service.port;

import com.testgenieai.backend.dto.DashboardSummaryResponse;
import com.testgenieai.backend.dto.DashboardTrendsResponse;

public interface DashboardReadService {

    DashboardSummaryResponse getDashboardSummary();

    DashboardTrendsResponse getDashboardTrends(Integer days);
}
