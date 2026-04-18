package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.SubscriptionHookRequest;
import com.testgenieai.backend.dto.SubscriptionHookResponse;
import com.testgenieai.backend.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/hooks/subscription")
    @PreAuthorize("hasRole('ADMIN')")
    public SubscriptionHookResponse subscriptionHook(@Valid @RequestBody SubscriptionHookRequest request) {
        return billingService.applySubscriptionHook(request);
    }
}
