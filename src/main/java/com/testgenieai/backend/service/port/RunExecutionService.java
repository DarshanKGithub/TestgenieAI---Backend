package com.testgenieai.backend.service.port;

import com.testgenieai.backend.dto.CreateTestRunRequest;
import com.testgenieai.backend.dto.PagedResponse;
import com.testgenieai.backend.dto.TestRunResponse;
import java.util.UUID;

public interface RunExecutionService {

    TestRunResponse executeTestRun(CreateTestRunRequest request);

    PagedResponse<TestRunResponse> getRuns(int page, int size, String status, String suiteName);

    TestRunResponse getRun(UUID runId);
}
