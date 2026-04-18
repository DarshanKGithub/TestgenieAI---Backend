package com.testgenieai.backend.runner;

import java.util.List;

public record PlaywrightRunOutput(
        List<PlaywrightTestCaseOutput> results
) {
}
