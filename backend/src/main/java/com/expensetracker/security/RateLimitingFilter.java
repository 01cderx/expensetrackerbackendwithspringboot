package com.expensetracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple fixed-window rate limiter for the login/register endpoints, to slow down
 * brute-force and mass-registration attempts. In-memory only — fine for a single
 * Render instance; would need a shared store (e.g. Redis) if scaled to multiple instances.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    private static class Window {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = Instant.now().getEpochSecond();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isLimitedEndpoint = path.equals("/api/auth/login") || path.equals("/api/auth/register");

        if (isLimitedEndpoint) {
            String key = clientIp(request) + ":" + path;
            Window window = windows.computeIfAbsent(key, k -> new Window());

            long now = Instant.now().getEpochSecond();
            synchronized (window) {
                if (now - window.windowStart >= WINDOW_SECONDS) {
                    window.windowStart = now;
                    window.count.set(0);
                }
                if (window.count.incrementAndGet() > MAX_REQUESTS_PER_WINDOW) {
                    response.setStatus(429); // Too Many Requests
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"message\":\"Too many attempts. Please wait a minute and try again.\"}"
                    );
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        // Render (and most PaaS) sit behind a proxy, so the real client IP
        // is forwarded in this header rather than being the socket's remote address.
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
