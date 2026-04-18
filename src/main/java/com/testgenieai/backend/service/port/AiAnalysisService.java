package com.testgenieai.backend.service.port;

import com.testgenieai.backend.dto.FailureAnalysisRequest;
import com.testgenieai.backend.dto.FailureAnalysisResponse;

public interface AiAnalysisService {

    FailureAnalysisResponse analyzeFailure(FailureAnalysisRequest request);
}
