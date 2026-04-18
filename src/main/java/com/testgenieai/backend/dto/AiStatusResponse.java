package com.testgenieai.backend.dto;

public record AiStatusResponse(
        boolean configured,
        String model,
        String baseUrl,
        String mode
) {
}
