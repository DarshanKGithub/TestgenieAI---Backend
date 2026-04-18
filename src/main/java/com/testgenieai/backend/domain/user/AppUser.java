package com.testgenieai.backend.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_tier", nullable = false)
    @Builder.Default
    private PlanTier planTier = PlanTier.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.TRIAL;

    @Column(name = "monthly_quota", nullable = false)
    @Builder.Default
    private Integer monthlyQuota = 50;

    @Column(name = "used_quota", nullable = false)
    @Builder.Default
    private Integer usedQuota = 0;

    @Column(name = "billing_cycle_start", nullable = false)
    @Builder.Default
    private LocalDateTime billingCycleStart = LocalDateTime.now();

    @Column(name = "billing_cycle_end", nullable = false)
    @Builder.Default
    private LocalDateTime billingCycleEnd = LocalDateTime.now().plusMonths(1);

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;
}
