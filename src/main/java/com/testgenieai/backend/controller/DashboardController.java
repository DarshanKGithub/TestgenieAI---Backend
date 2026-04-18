package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.DashboardTrendsResponse;
import com.testgenieai.backend.dto.DashboardSummaryResponse;
import com.testgenieai.backend.service.port.DashboardReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardReadService dashboardReadService;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return dashboardReadService.getDashboardSummary();
    }

    @GetMapping("/trends")
    public DashboardTrendsResponse trends(@RequestParam(required = false) Integer days) {
        return dashboardReadService.getDashboardTrends(days);
    }
}
