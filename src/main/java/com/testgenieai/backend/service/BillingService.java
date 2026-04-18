package com.testgenieai.backend.service;

import com.testgenieai.backend.domain.user.AppUser;
import com.testgenieai.backend.dto.SubscriptionHookRequest;
import com.testgenieai.backend.dto.SubscriptionHookResponse;
import com.testgenieai.backend.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final AppUserRepository appUserRepository;

    @Transactional
    public SubscriptionHookResponse applySubscriptionHook(SubscriptionHookRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("User not found for billing hook: " + request.email()));

        user.setPlanTier(request.planTier());
        user.setSubscriptionStatus(request.subscriptionStatus());
        user.setMonthlyQuota(request.monthlyQuota());
        user.setStripeCustomerId(request.stripeCustomerId());

        if (user.getBillingCycleStart() == null || user.getBillingCycleEnd() == null) {
            user.setBillingCycleStart(LocalDateTime.now());
            user.setBillingCycleEnd(LocalDateTime.now().plusMonths(1));
        }

        if (request.monthlyQuota() != null && request.monthlyQuota() >= 0) {
            user.setUsedQuota(Math.min(user.getUsedQuota() == null ? 0 : user.getUsedQuota(), request.monthlyQuota()));
        }

        appUserRepository.save(user);

        return new SubscriptionHookResponse(
                user.getEmail(),
                user.getPlanTier(),
                user.getSubscriptionStatus(),
                user.getMonthlyQuota(),
                "Subscription hook processed successfully"
        );
    }
}
