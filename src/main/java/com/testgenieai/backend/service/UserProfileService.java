package com.testgenieai.backend.service;

import com.testgenieai.backend.domain.user.AppUser;
import com.testgenieai.backend.dto.UserProfileResponse;
import com.testgenieai.backend.repository.AppUserRepository;
import com.testgenieai.backend.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse currentUserProfile() {
        AppUser user = appUserRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> listUsers() {
        return appUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private UserProfileResponse toResponse(AppUser user) {
        String plan = user.getPlanTier() == null ? "FREE" : user.getPlanTier().name();
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getRole(), plan);
    }
}
