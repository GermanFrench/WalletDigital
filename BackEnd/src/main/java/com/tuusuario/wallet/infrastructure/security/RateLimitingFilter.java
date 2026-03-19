package com.tuusuario.wallet.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro de rate limiting en memoria para endpoints sensibles de autenticación.
 * Límites por IP: /api/auth/login y /register → 10 req/min; /forgot-password → 5 req/min.
 */
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/forgot-password"
    );

    private static final Map<String, Integer> PATH_LIMITS = Map.of(
            "/api/auth/login", 10,
            "/api/auth/register", 5,
            "/api/auth/refresh", 20,
            "/api/auth/forgot-password", 3
    );

    // ip:path → entry
    private final ConcurrentHashMap<String, RateLimitEntry> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (LIMITED_PATHS.contains(path)) {
            String ip = resolveClientIp(request);
            String key = ip + ":" + path;
            int limit = PATH_LIMITS.getOrDefault(path, 10);

            RateLimitEntry entry = requestCounts.compute(key, (k, existing) -> {
                long now = System.currentTimeMillis();
                if (existing == null || now - existing.windowStart > 60_000L) {
                    return new RateLimitEntry(new AtomicInteger(1), now);
                }
                existing.count.incrementAndGet();
                return existing;
            });

            if (entry.count.get() > limit) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Too Many Requests\","
                        + "\"message\":\"Demasiadas solicitudes. Intenta en un minuto.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record RateLimitEntry(AtomicInteger count, long windowStart) {
    }
}
