package com.testgenieai.backend.service;

import com.testgenieai.backend.ai.GroqAiService;
import com.testgenieai.backend.domain.ExecutionStatus;
import com.testgenieai.backend.domain.TestCaseResult;
import com.testgenieai.backend.domain.TestRun;
import com.testgenieai.backend.domain.user.AppUser;
import com.testgenieai.backend.dto.CreateTestRunRequest;
import com.testgenieai.backend.dto.DashboardTrendsResponse;
import com.testgenieai.backend.dto.DashboardSummaryResponse;
import com.testgenieai.backend.dto.DurationTrendPointResponse;
import com.testgenieai.backend.dto.FailureAnalysisRequest;
import com.testgenieai.backend.dto.FailureAnalysisResponse;
import com.testgenieai.backend.dto.GenerateTestCasesRequest;
import com.testgenieai.backend.dto.GenerateTestCasesResponse;
import com.testgenieai.backend.dto.PagedResponse;
import com.testgenieai.backend.dto.PassFailTrendPointResponse;
import com.testgenieai.backend.dto.TestCaseResultRequest;
import com.testgenieai.backend.dto.TestCaseResultResponse;
import com.testgenieai.backend.dto.TestRunResponse;
import com.testgenieai.backend.repository.AppUserRepository;
import com.testgenieai.backend.repository.TestCaseResultRepository;
import com.testgenieai.backend.repository.TestRunRepository;
import com.testgenieai.backend.runner.PlaywrightRunInput;
import com.testgenieai.backend.runner.PlaywrightTestCaseInput;
import com.testgenieai.backend.security.SecurityUtils;
import com.testgenieai.backend.service.port.AiAnalysisService;
import com.testgenieai.backend.service.port.DashboardReadService;
import com.testgenieai.backend.service.port.RunExecutionService;
import com.testgenieai.backend.service.port.TestCaseGenerationService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TestRunService implements RunExecutionService, DashboardReadService, TestCaseGenerationService, AiAnalysisService {

    private static final int DEFAULT_RECENT_DAYS = 14;

    private final TestRunRepository testRunRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final AppUserRepository appUserRepository;
    private final TestExecutionDispatcherService testExecutionDispatcherService;
    private final GroqAiService groqAiService;
    private final UsageQuotaService usageQuotaService;

    @Transactional
    public TestRunResponse executeTestRun(CreateTestRunRequest request) {
        AppUser owner = appUserRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        usageQuotaService.consumeRunQuota(owner);

        TestRun run = TestRun.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .suiteName(request.suiteName())
                .targetUrl(request.targetUrl())
                .status(ExecutionStatus.PENDING)
                .startedAt(java.time.LocalDateTime.now())
                .finishedAt(null)
                .totalTests(request.testCases().size())
                .passedTests(0)
                .failedTests(0)
                .durationMs(null)
                .triggeredBy("MANUAL")
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
        return mapToResponse(savedRun, testCaseResultRepository.findByTestRunId(savedRun.getId()));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TestRunResponse> getRuns(int page, int size, String status, String suiteName) {
        UUID ownerId = SecurityUtils.currentUserId();
        ExecutionStatus filterStatus = parseFilterStatus(status);
        String suiteFilter = (suiteName == null || suiteName.isBlank()) ? null : suiteName;

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(safePage, safeSize);

        Page<TestRun> runsPage;
        if (filterStatus == null && suiteFilter == null) {
            runsPage = testRunRepository.findByOwnerIdOrderByStartedAtDesc(ownerId, pageable);
        } else if (filterStatus != null && suiteFilter == null) {
            runsPage = testRunRepository.findByOwnerIdAndStatusOrderByStartedAtDesc(ownerId, filterStatus, pageable);
        } else if (filterStatus == null) {
            runsPage = testRunRepository.findByOwnerIdAndSuiteNameContainingIgnoreCaseOrderByStartedAtDesc(ownerId, suiteFilter, pageable);
        } else {
            runsPage = testRunRepository.findByOwnerIdAndStatusAndSuiteNameContainingIgnoreCaseOrderByStartedAtDesc(
                    ownerId,
                    filterStatus,
                    suiteFilter,
                    pageable
            );
        }

        List<TestRunResponse> items = runsPage.getContent().stream()
                .map(run -> mapToResponse(run, testCaseResultRepository.findByTestRunId(run.getId())))
                .toList();

        return new PagedResponse<>(
            items,
            runsPage.getNumber(),
            runsPage.getSize(),
            runsPage.getTotalElements(),
            runsPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public TestRunResponse getRun(UUID runId) {
        UUID ownerId = SecurityUtils.currentUserId();
        TestRun run = testRunRepository.findById(runId)
            .filter(item -> item.getOwner().getId().equals(ownerId))
            .orElseThrow(() -> new EntityNotFoundException("Test run not found: " + runId));
        return mapToResponse(run, testCaseResultRepository.findByTestRunId(run.getId()));
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        UUID ownerId = SecurityUtils.currentUserId();
        List<TestRun> runs = testRunRepository.findByOwnerIdOrderByStartedAtDesc(ownerId, PageRequest.of(0, 200)).getContent();
        long total = runs.size();
        long successful = runs.stream().filter(run -> run.getStatus() == ExecutionStatus.PASSED).count();
        long failed = runs.stream().filter(run -> run.getStatus() == ExecutionStatus.FAILED).count();

        double passRate = total == 0 ? 0.0 : (successful * 100.0) / total;
        long avgDuration = (long) runs.stream()
                .map(TestRun::getDurationMs)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        return new DashboardSummaryResponse(total, successful, failed, passRate, avgDuration);
    }

    @Transactional(readOnly = true)
    public DashboardTrendsResponse getDashboardTrends(Integer days) {
        int rangeDays = days == null || days <= 0 ? DEFAULT_RECENT_DAYS : days;
        UUID ownerId = SecurityUtils.currentUserId();
        List<TestRun> runs = testRunRepository.findByOwnerIdOrderByStartedAtDesc(ownerId, PageRequest.of(0, 500)).getContent();

        LocalDate startDate = LocalDate.now().minusDays(rangeDays - 1L);

        Map<LocalDate, List<TestRun>> grouped = new LinkedHashMap<>();
        for (int i = 0; i < rangeDays; i++) {
            grouped.put(startDate.plusDays(i), new ArrayList<>());
        }

        for (TestRun run : runs) {
            LocalDate date = run.getStartedAt().toLocalDate();
            if (!date.isBefore(startDate)) {
                grouped.computeIfAbsent(date, ignored -> new ArrayList<>()).add(run);
            }
        }

        List<PassFailTrendPointResponse> passFailTrend = grouped.entrySet().stream()
                .map(entry -> {
                    long passed = entry.getValue().stream().filter(run -> run.getStatus() == ExecutionStatus.PASSED).count();
                    long failed = entry.getValue().stream().filter(run -> run.getStatus() == ExecutionStatus.FAILED).count();
                    return new PassFailTrendPointResponse(entry.getKey(), passed, failed);
                })
                .toList();

        List<DurationTrendPointResponse> durationTrend = grouped.entrySet().stream()
                .map(entry -> {
                    long avg = (long) entry.getValue().stream()
                            .map(TestRun::getDurationMs)
                            .filter(Objects::nonNull)
                            .mapToLong(Long::longValue)
                            .average()
                            .orElse(0.0);
                    return new DurationTrendPointResponse(entry.getKey(), avg);
                })
                .toList();

        return new DashboardTrendsResponse(passFailTrend, durationTrend);
    }

    public GenerateTestCasesResponse generateTestCases(GenerateTestCasesRequest request) {
        return groqAiService.generateTestCases(request)
            .orElseGet(() -> generateFallbackTestCases(request));
    }

    public FailureAnalysisResponse analyzeFailure(FailureAnalysisRequest request) {
        return groqAiService.analyzeFailure(request)
            .orElseGet(() -> new FailureAnalysisResponse(
                "AI analysis unavailable. Returning rule-based summary.",
                inferProbableCauses(request.errorLog()),
                inferSuggestedFixes(request.errorLog())
            ));
    }

    private GenerateTestCasesResponse generateFallbackTestCases(GenerateTestCasesRequest request) {
        List<String> flowSteps = List.of(request.userFlow().split("\\r?\\n"))
                .stream()
                .map(String::trim)
                .filter(step -> !step.isBlank())
                .toList();

        List<TestCaseResultRequest> generated = new ArrayList<>();
        for (int i = 0; i < flowSteps.size(); i++) {
            String step = flowSteps.get(i);
            String slug = step.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
            String path = "/" + (slug.isBlank() ? "" : slug);
            generated.add(new TestCaseResultRequest(
                    "Flow Step " + (i + 1) + ": " + step,
                    path,
                    null
            ));
            generated.add(new TestCaseResultRequest(
                    "Negative Step " + (i + 1) + ": " + step + " invalid access",
                    path + "?invalid=true",
                    null
            ));
        }

        if (generated.isEmpty()) {
            generated.add(new TestCaseResultRequest("Open Home Page", "/", null));
        }

        String suiteName = "Generated Suite - " + LocalDate.now();
        return new GenerateTestCasesResponse(suiteName, request.targetUrl(), generated);
    }

    private List<String> inferProbableCauses(String errorLog) {
        String lower = errorLog.toLowerCase(Locale.ROOT);
        List<String> causes = new ArrayList<>();
        if (lower.contains("timeout") || lower.contains("timed out")) {
            causes.add("Element or API response exceeded expected timeout window.");
        }
        if (lower.contains("selector") || lower.contains("locator") || lower.contains("not found")) {
            causes.add("UI locator may have changed or target element is not rendered yet.");
        }
        if (lower.contains("401") || lower.contains("403") || lower.contains("unauthorized")) {
            causes.add("Authentication or authorization token may be invalid/expired.");
        }
        if (lower.contains("assert") || lower.contains("expected")) {
            causes.add("Assertion expectation no longer matches current behavior.");
        }
        if (causes.isEmpty()) {
            causes.add("Potential data setup mismatch or environment instability.");
        }
        return causes;
    }

    private List<String> inferSuggestedFixes(String errorLog) {
        String lower = errorLog.toLowerCase(Locale.ROOT);
        List<String> fixes = new ArrayList<>();
        if (lower.contains("timeout") || lower.contains("timed out")) {
            fixes.add("Add explicit waits for network idle or stable element visibility before assertions.");
        }
        if (lower.contains("selector") || lower.contains("locator") || lower.contains("not found")) {
            fixes.add("Use resilient locators (role, label, test-id) and update outdated selectors.");
        }
        if (lower.contains("401") || lower.contains("403") || lower.contains("unauthorized")) {
            fixes.add("Refresh auth token and verify role permissions for the tested action.");
        }
        if (lower.contains("assert") || lower.contains("expected")) {
            fixes.add("Review expected values and align assertions with intended behavior.");
        }
        if (fixes.isEmpty()) {
            fixes.add("Re-run with debug logs/screenshots and isolate the smallest reproducible failing step.");
        }
        return fixes;
    }

    private TestRunResponse mapToResponse(TestRun run, List<TestCaseResult> testCases) {
        List<TestCaseResultResponse> caseResponses = testCases.stream()
                .map(result -> new TestCaseResultResponse(
                        result.getId(),
                        result.getTestName(),
                        result.getStatus(),
                        result.getErrorMessage(),
                        result.getDurationMs(),
                        result.getExecutedAt()))
                .toList();

        return new TestRunResponse(
                run.getId(),
                run.getSuiteName(),
            run.getTargetUrl(),
                run.getStatus(),
                run.getStartedAt(),
                run.getFinishedAt(),
                run.getTotalTests(),
                run.getPassedTests(),
                run.getFailedTests(),
                run.getDurationMs(),
                caseResponses
        );
    }

    private ExecutionStatus parseFilterStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ExecutionStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status filter: " + status);
        }
    }
}
