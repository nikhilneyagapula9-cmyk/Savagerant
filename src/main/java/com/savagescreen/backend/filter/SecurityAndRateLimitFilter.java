package com.savagescreen.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SecurityAndRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MILLIS = 60_000;
    private static final int LOGIN_LIMIT = 10;
    private static final int CHAT_LIMIT = 20;
    private static final int WRITE_LIMIT = 60;
    private static final int READ_LIMIT = 240;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        addSecurityHeaders(response);
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            int limit = limitFor(request);
            String key = request.getRemoteAddr() + ":" + limitClass(request);
            Window window = windows.compute(key, (ignored, current) -> current == null || current.expired()
                    ? new Window(Instant.now().toEpochMilli()) : current);
            int count = window.count.incrementAndGet();
            response.setHeader("RateLimit-Limit", Integer.toString(limit));
            response.setHeader("RateLimit-Remaining", Integer.toString(Math.max(0, limit - count)));
            if (count > limit) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Please wait a minute before trying again.\"}");
                return;
            }
            if (windows.size() > 10_000) {
                long now = Instant.now().toEpochMilli();
                windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt >= WINDOW_MILLIS);
            }
        }
        chain.doFilter(request, response);
    }

    private int limitFor(HttpServletRequest request) {
        if (request.getRequestURI().equals("/api/auth/login") || request.getRequestURI().equals("/api/auth/register")) {
            return LOGIN_LIMIT;
        }
        if (request.getRequestURI().equals("/api/assistant/chat")) {
            return CHAT_LIMIT;
        }
        return "GET".equalsIgnoreCase(request.getMethod()) ? READ_LIMIT : WRITE_LIMIT;
    }

    private String limitClass(HttpServletRequest request) {
        if (request.getRequestURI().equals("/api/auth/login") || request.getRequestURI().equals("/api/auth/register")) return "auth";
        if (request.getRequestURI().equals("/api/assistant/chat")) return "chat";
        return "GET".equalsIgnoreCase(request.getMethod()) ? "read" : "write";
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'; " +
                "object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'");
    }

    private static final class Window {
        private final long startedAt;
        private final AtomicInteger count = new AtomicInteger();

        private Window(long startedAt) { this.startedAt = startedAt; }
        private boolean expired() { return Instant.now().toEpochMilli() - startedAt >= WINDOW_MILLIS; }
    }
}
