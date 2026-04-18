package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.UserProfileResponse;
import com.testgenieai.backend.service.UserProfileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public UserProfileResponse me() {
        return userProfileService.currentUserProfile();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileResponse> listUsers() {
        return userProfileService.listUsers();
    }
}
