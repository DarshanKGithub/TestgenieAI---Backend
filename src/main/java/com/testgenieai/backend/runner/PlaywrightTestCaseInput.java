package com.testgenieai.backend.runner;

public record PlaywrightTestCaseInput(
        String testName,
        String path,
        String expectedText
) {
}
