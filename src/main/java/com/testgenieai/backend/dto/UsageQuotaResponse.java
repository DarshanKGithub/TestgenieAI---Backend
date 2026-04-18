package com.testgenieai.backend.dto;

import com.testgenieai.backend.domain.user.PlanTier;
import com.testgenieai.backend.domain.user.SubscriptionStatus;
import java.time.LocalDateTime;

public record UsageQuotaResponse(
        PlanTier planTier,
        SubscriptionStatus subscriptionStatus,
        int monthlyQuota,
        int usedQuota,
        int remainingQuota,
        LocalDateTime billingCycleStart,
        LocalDateTime billingCycleEnd
) {
}
