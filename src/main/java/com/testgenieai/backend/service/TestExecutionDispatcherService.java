package com.testgenieai.backend.service;

import com.testgenieai.backend.domain.ExecutionStatus;
import com.testgenieai.backend.domain.TestCaseResult;
import com.testgenieai.backend.domain.TestRun;
import com.testgenieai.backend.repository.TestCaseResultRepository;
import com.testgenieai.backend.repository.TestRunRepository;
import com.testgenieai.backend.runner.PlaywrightRunInput;
import com.testgenieai.backend.runner.PlaywrightRunOutput;
import com.testgenieai.backend.runner.PlaywrightRunnerService;
import com.testgenieai.backend.runner.PlaywrightTestCaseOutput;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestExecutionDispatcherService {

    private final TestRunRepository testRunRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final PlaywrightRunnerService playwrightRunnerService;

    @Async("testExecutionExecutor")
    @Transactional
    public void dispatch(UUID runId, PlaywrightRunInput input) {
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalStateException("Test run not found: " + runId));

        run.setStatus(ExecutionStatus.RUNNING);
        run.setStartedAt(LocalDateTime.now());
        testRunRepository.save(run);

        LocalDateTime executionStart = LocalDateTime.now();
        try {
            PlaywrightRunOutput runOutput = playwrightRunnerService.execute(input);
            persistResults(run, runOutput.results());
            finishRun(run, executionStart, ExecutionStatus.PASSED, null);
        } catch (Exception ex) {
            TestCaseResult failureResult = TestCaseResult.builder()
                    .id(UUID.randomUUID())
                    .testRun(run)
                    .testName("Execution Pipeline")
                    .status(ExecutionStatus.FAILED)
                    .errorMessage(ex.getMessage())
                    .durationMs(0L)
                    .executedAt(LocalDateTime.now())
                    .build();
            testCaseResultRepository.save(failureResult);
            finishRun(run, executionStart, ExecutionStatus.FAILED, ex.getMessage());
        }
    }

    private void persistResults(TestRun run, List<PlaywrightTestCaseOutput> outputs) {
        int passedCount = 0;
        int failedCount = 0;

        for (PlaywrightTestCaseOutput output : outputs) {
            ExecutionStatus status = parseExecutionStatus(output.status());
            if (status == ExecutionStatus.PASSED) {
                passedCount++;
            } else {
                failedCount++;
            }

            TestCaseResult result = TestCaseResult.builder()
                    .id(UUID.randomUUID())
                    .testRun(run)
                    .testName(output.testName())
                    .status(status)
                    .errorMessage(output.errorMessage())
                    .durationMs(output.durationMs())
                    .executedAt(LocalDateTime.now())
                    .build();
            testCaseResultRepository.save(result);
        }

        run.setPassedTests(passedCount);
        run.setFailedTests(failedCount);
        run.setStatus(failedCount == 0 ? ExecutionStatus.PASSED : ExecutionStatus.FAILED);
    }

    private void finishRun(TestRun run, LocalDateTime executionStart, ExecutionStatus status, String message) {
        run.setFinishedAt(LocalDateTime.now());
        run.setDurationMs(Duration.between(executionStart, run.getFinishedAt()).toMillis());
        run.setStatus(status);
        testRunRepository.save(run);
    }

    private ExecutionStatus parseExecutionStatus(String status) {
        try {
            return ExecutionStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return ExecutionStatus.FAILED;
        }
    }
}
