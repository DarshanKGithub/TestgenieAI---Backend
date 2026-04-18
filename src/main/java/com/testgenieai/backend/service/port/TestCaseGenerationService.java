package com.testgenieai.backend.service.port;

import com.testgenieai.backend.dto.GenerateTestCasesRequest;
import com.testgenieai.backend.dto.GenerateTestCasesResponse;

public interface TestCaseGenerationService {

    GenerateTestCasesResponse generateTestCases(GenerateTestCasesRequest request);
}
