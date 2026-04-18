package com.testgenieai.backend.dto;

import java.util.List;

public record GenerateTestCasesResponse(
        String suiteName,
        String targetUrl,
        List<TestCaseResultRequest> testCases
) {
}
