package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.UsageQuotaResponse;
import com.testgenieai.backend.service.UsageQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageQuotaService usageQuotaService;

    @GetMapping("/me")
    public UsageQuotaResponse me() {
        return usageQuotaService.currentUserUsage();
    }

    @PostMapping("/me/reset-cycle")
    public UsageQuotaResponse resetCycle() {
        return usageQuotaService.resetCurrentCycle();
    }
}
