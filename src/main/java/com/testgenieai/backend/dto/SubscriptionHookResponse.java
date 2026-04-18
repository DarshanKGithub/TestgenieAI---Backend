package com.testgenieai.backend.dto;

import com.testgenieai.backend.domain.user.PlanTier;
import com.testgenieai.backend.domain.user.SubscriptionStatus;

public record SubscriptionHookResponse(
        String email,
        PlanTier planTier,
        SubscriptionStatus subscriptionStatus,
        int monthlyQuota,
        String message
) {
}
