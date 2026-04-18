package com.testgenieai.backend.dto;

import com.testgenieai.backend.domain.user.PlanTier;
import com.testgenieai.backend.domain.user.SubscriptionStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscriptionHookRequest(
        @Email @NotBlank String email,
        @NotNull PlanTier planTier,
        @NotNull SubscriptionStatus subscriptionStatus,
        @NotNull Integer monthlyQuota,
        String stripeCustomerId
) {
}
