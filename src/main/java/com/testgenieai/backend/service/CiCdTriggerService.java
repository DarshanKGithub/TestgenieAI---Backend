package com.testgenieai.backend.service;

import com.testgenieai.backend.domain.ExecutionStatus;
import com.testgenieai.backend.domain.TestRun;
import com.testgenieai.backend.domain.user.AppUser;
import com.testgenieai.backend.dto.CiCdTriggerRequest;
import com.testgenieai.backend.dto.CiCdTriggerResponse;
import com.testgenieai.backend.repository.AppUserRepository;
import com.testgenieai.backend.repository.TestRunRepository;
import com.testgenieai.backend.runner.PlaywrightRunInput;
import com.testgenieai.backend.runner.PlaywrightTestCaseInput;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CiCdTriggerService {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TestRunRepository testRunRepository;
    private final AppUserRepository appUserRepository;
    private final TestExecutionDispatcherService testExecutionDispatcherService;
    private final UsageQuotaService usageQuotaService;

    @Value("${cicd.webhook.secret:testgenie-ci-secret}")
    private String configuredSecret;

    @Transactional
    public CiCdTriggerResponse trigger(CiCdTriggerRequest request, String providedSecret) {
        if (providedSecret == null || !providedSecret.equals(configuredSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid CI/CD webhook secret");
        }

        AppUser owner = resolveOwner(request.ownerEmail());
        usageQuotaService.consumeRunQuota(owner);

        TestRun run = TestRun.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .suiteName(request.suiteName())
                .targetUrl(request.targetUrl())
                .status(ExecutionStatus.PENDING)
                .startedAt(LocalDateTime.now())
                .finishedAt(null)
                .totalTests(request.testCases().size())
                .passedTests(0)
                .failedTests(0)
                .durationMs(null)
                .triggeredBy("CI_CD")
                .ciProject(request.project())
                .ciBranch(request.branch())
                .ciCommitSha(request.commitSha())
                .build();

        TestRun savedRun = testRunRepository.saveAndFlush(run);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                testExecutionDispatcherService.dispatch(savedRun.getId(), new PlaywrightRunInput(
                        request.targetUrl(),
                        request.testCases().stream()
                                .map(testCase -> new PlaywrightTestCaseInput(testCase.testName(), testCase.path(), testCase.expectedText()))
                                .toList()
                ));
            }
        });

        return new CiCdTriggerResponse(savedRun.getId(), savedRun.getStatus().name(), request.project(), request.branch(), request.commitSha());
    }

    private AppUser resolveOwner(String ownerEmail) {
        if (ownerEmail != null && !ownerEmail.isBlank()) {
            return appUserRepository.findByEmail(ownerEmail.toLowerCase())
                    .orElseThrow(() -> new EntityNotFoundException("Owner not found: " + ownerEmail));
        }

        return appUserRepository.findById(SYSTEM_USER_ID)
                .orElseThrow(() -> new EntityNotFoundException("System user not found"));
    }
}
