package com.testgenieai.backend.dto;

import java.util.List;

public record FailureAnalysisResponse(
        String summary,
        List<String> probableCauses,
        List<String> suggestedFixes
) {
}
