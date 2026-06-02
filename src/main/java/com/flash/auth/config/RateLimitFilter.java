package com.flash.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录接口限流过滤器
 * 每个 IP 每 60 秒最多 5 次登录尝试，防暴力破解
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, RateLimitEntry> cache = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 60_000L;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        if (!"POST".equalsIgnoreCase(req.getMethod()) || !req.getRequestURI().contains("/api/auth/login")) {
            chain.doFilter(req, resp);
            return;
        }

        String ip = getClientIp(req);
        long now = System.currentTimeMillis();
        int maxAttempts = 5;

        RateLimitEntry entry = cache.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new RateLimitEntry(now, 1);
            }
            existing.count++;
            return existing;
        });

        if (entry.count > maxAttempts) {
            resp.setStatus(429);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"code\":429,\"msg\":\"登录尝试过于频繁，请 60 秒后重试\",\"data\":null}");
            return;
        }

        chain.doFilter(req, resp);
    }

    @Scheduled(fixedRate = 300_000)
    public void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        Iterator<java.util.Map.Entry<String, RateLimitEntry>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<String, RateLimitEntry> e = it.next();
            if (now - e.getValue().windowStart > WINDOW_MS * 2) {
                it.remove();
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty() && !"unknown".equalsIgnoreCase(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitEntry {
        long windowStart;
        int count;

        RateLimitEntry(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
