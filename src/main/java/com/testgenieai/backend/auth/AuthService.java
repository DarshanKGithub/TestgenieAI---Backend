package com.testgenieai.backend.auth;

import com.testgenieai.backend.domain.user.AppUser;
import com.testgenieai.backend.domain.user.UserRole;
import com.testgenieai.backend.repository.AppUserRepository;
import com.testgenieai.backend.security.JwtService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${security.registration.admin-invite-code:}")
    private String adminInviteCode;

    public AuthResponse register(RegisterRequest request) {
        appUserRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        });

        UserRole role = request.role() == null ? UserRole.MEMBER : request.role();
        if (role == UserRole.ADMIN) {
            if (adminInviteCode == null || adminInviteCode.isBlank() || request.inviteCode() == null || !adminInviteCode.equals(request.inviteCode())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN registration requires a valid invite code");
            }
        }

        AppUser user = AppUser.builder()
                .id(UUID.randomUUID())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .role(role)
                .build();

        AppUser saved = appUserRepository.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());
        return new AuthResponse(token, saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail());
    }
}
