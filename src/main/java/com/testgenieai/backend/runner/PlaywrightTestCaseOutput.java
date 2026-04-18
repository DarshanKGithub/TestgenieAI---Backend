package com.testgenieai.backend.runner;

public record PlaywrightTestCaseOutput(
        String testName,
        String status,
        String errorMessage,
        long durationMs,
        String screenshotPath
) {
}
