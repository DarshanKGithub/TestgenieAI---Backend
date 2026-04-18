package com.testgenieai.backend.dto;

import java.util.UUID;

public record CiCdTriggerResponse(
        UUID runId,
        String status,
        String project,
        String branch,
        String commitSha
) {
}
