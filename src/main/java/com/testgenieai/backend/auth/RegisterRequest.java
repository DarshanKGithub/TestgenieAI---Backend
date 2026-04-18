package com.testgenieai.backend.auth;

import com.testgenieai.backend.domain.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        UserRole role,
        String inviteCode
) {
}
