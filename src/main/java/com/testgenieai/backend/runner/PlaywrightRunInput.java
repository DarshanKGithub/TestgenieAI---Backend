package com.testgenieai.backend.runner;

import java.util.List;

public record PlaywrightRunInput(
        String targetUrl,
        List<PlaywrightTestCaseInput> testCases
) {
}
