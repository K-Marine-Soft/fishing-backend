package com.kmarine.fishing.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int    MAX_REQUESTS = 60;  // 분당 60회
    private static final long   WINDOW       = 60;  // 60초

    @Override
    public void doFilter(ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;
        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // 로컬 개발 환경은 속도 제한 제외
        if (isLocalRequest(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // 로그인/회원가입은 더 엄격하게 (분당 10회)
        if (path.contains("/api/auth/login")
         || path.contains("/api/auth/signup")) {
            String ip = getClientIp(httpRequest);
            String key = "ratelimit:auth:" + ip;

            if (!checkLimit(key, 10, WINDOW)) {
                httpResponse.setStatus(429);
                httpResponse.getWriter().write(
                    "{\"success\":false,\"message\":" +
                    "\"너무 많은 요청입니다. 잠시 후 다시 시도해주세요.\"}");
                return;
            }
        } else if (path.startsWith("/api/")) {
            String ip = getClientIp(httpRequest);
            String key = "ratelimit:api:" + ip;

            if (!checkLimit(key, MAX_REQUESTS, WINDOW)) {
                httpResponse.setStatus(429);
                httpResponse.getWriter().write(
                    "{\"success\":false,\"message\":" +
                    "\"요청 한도를 초과했습니다.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean checkLimit(String key, int max,
                                long windowSeconds) {
        Long count = redisTemplate.opsForValue()
                .increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, windowSeconds,
                                  TimeUnit.SECONDS);
        }
        return count == null || count <= max;
    }

    private boolean isLocalRequest(HttpServletRequest request) {
        // X-Forwarded-For가 없을 때만 (프록시 뒤에서는 실제 클라이언트 IP로 정상 검사)
        if (request.getHeader("X-Forwarded-For") != null) {
            return false;
        }
        String ip = request.getRemoteAddr();
        return "127.0.0.1".equals(ip)
            || "0:0:0:0:0:0:0:1".equals(ip)
            || "::1".equals(ip);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}