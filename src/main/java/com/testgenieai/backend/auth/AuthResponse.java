package com.testgenieai.backend.auth;

public record AuthResponse(
        String token,
        String email
) {
}
