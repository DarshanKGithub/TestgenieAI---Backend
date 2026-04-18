package com.testgenieai.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final int WINDOW_SECONDS = 60;
    private static final DateTimeFormatter WINDOW_FORMAT = DateTimeFormatter.ISO_INSTANT;

    private final Map<String, RateLimitWindow> windows = new ConcurrentHashMap<>();

    @Value("${security.rate-limit.requests-per-window:120}")
    private int requestsPerWindow;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/") || path.equals("/api/v1/ai/status");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long windowStart = currentWindowStart();
        String key = resolveKey(request);
        String bucket = key + ":" + windowStart;

        RateLimitWindow counter = windows.computeIfAbsent(bucket, ignored -> new RateLimitWindow(windowStart));
        int currentCount = counter.count.incrementAndGet();
        cleanupExpiredWindows(windowStart);

        int remaining = Math.max(requestsPerWindow - currentCount, 0);
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerWindow));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Window-Start", WINDOW_FORMAT.format(Instant.ofEpochSecond(windowStart).atOffset(ZoneOffset.UTC)));

        if (currentCount > requestsPerWindow) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser principal) {
            return "user:" + principal.id();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return "ip:" + forwarded.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private long currentWindowStart() {
        return Instant.now().getEpochSecond() / WINDOW_SECONDS * WINDOW_SECONDS;
    }

    private void cleanupExpiredWindows(long activeWindowStart) {
        windows.entrySet().removeIf(entry -> entry.getValue().windowStart < activeWindowStart);
    }

    private static final class RateLimitWindow {
        private final long windowStart;
        private final AtomicInteger count = new AtomicInteger(0);

        private RateLimitWindow(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
