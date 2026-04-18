package com.testgenieai.backend.dto;

import com.testgenieai.backend.domain.user.UserRole;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        UserRole role,
        String plan
) {
}
