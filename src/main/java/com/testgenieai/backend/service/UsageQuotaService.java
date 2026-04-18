package com.testgenieai.backend.service;

import com.testgenieai.backend.domain.user.AppUser;
import com.testgenieai.backend.dto.UsageQuotaResponse;
import com.testgenieai.backend.repository.AppUserRepository;
import com.testgenieai.backend.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsageQuotaService {

    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public UsageQuotaResponse currentUserUsage() {
        AppUser user = appUserRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
        return toResponse(user);
    }

    @Transactional
    public void consumeRunQuota(AppUser user) {
        rotateCycleIfNeeded(user);
        int monthlyQuota = safeInt(user.getMonthlyQuota(), 0);
        int usedQuota = safeInt(user.getUsedQuota(), 0);
        if (usedQuota >= monthlyQuota) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Monthly quota exceeded. Upgrade your subscription or wait for cycle reset.");
        }
        user.setUsedQuota(usedQuota + 1);
        appUserRepository.save(user);
    }

    @Transactional
    public UsageQuotaResponse resetCurrentCycle() {
        AppUser user = appUserRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
        user.setUsedQuota(0);
        user.setBillingCycleStart(LocalDateTime.now());
        user.setBillingCycleEnd(LocalDateTime.now().plusMonths(1));
        appUserRepository.save(user);
        return toResponse(user);
    }

    private void rotateCycleIfNeeded(AppUser user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getBillingCycleEnd() != null && now.isAfter(user.getBillingCycleEnd())) {
            user.setUsedQuota(0);
            user.setBillingCycleStart(now);
            user.setBillingCycleEnd(now.plusMonths(1));
        }
    }

    private UsageQuotaResponse toResponse(AppUser user) {
        int quota = safeInt(user.getMonthlyQuota(), 0);
        int used = safeInt(user.getUsedQuota(), 0);
        int remaining = Math.max(quota - used, 0);
        return new UsageQuotaResponse(
                user.getPlanTier(),
                user.getSubscriptionStatus(),
                quota,
                used,
                remaining,
                user.getBillingCycleStart(),
                user.getBillingCycleEnd()
        );
    }

    private int safeInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
