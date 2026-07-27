package com.kmarine.fishing.auth;

import com.kmarine.fishing.config.JwtUtil;
import com.kmarine.fishing.fleet.FleetAdminMappingRepository;
import com.kmarine.fishing.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final FleetAdminMappingRepository fleetAdminMappingRepository;


    
    @Value("${frontend.url:http://localhost:5173}")
    private static String frontendUrl;
    
    // 로그인 성공 후 리다이렉트 URL
    private static final String REDIRECT_URI =
    		frontendUrl + "/oauth2/callback";
    
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2UserDetails oAuth2User =
                (OAuth2UserDetails) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // JWT 토큰 생성
        String accessToken  = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId());

        // Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                7, TimeUnit.DAYS);

        Long fleetId = fleetAdminMappingRepository
                .findByUserId(user.getId())
                .map(m -> m.getFleet().getId())
                .orElse(null);

        // 프론트로 토큰 전달 (쿼리 파라미터)
        UriComponentsBuilder redirectBuilder = UriComponentsBuilder
                .fromUriString(REDIRECT_URI)
                .queryParam("accessToken",  accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("userId",       user.getId())
                .queryParam("name",         URLEncoder.encode(user.getName(), StandardCharsets.UTF_8))
                .queryParam("email",        user.getEmail())
                .queryParam("role",         user.getRole().name());
        if (fleetId != null) {
            redirectBuilder.queryParam("fleetId", fleetId);
        }
        String redirectUrl = redirectBuilder.build().toUriString();

        log.info("OAuth2 로그인 성공 리다이렉트: {}",
                 user.getEmail());
        getRedirectStrategy().sendRedirect(
                request, response, redirectUrl);
    }
}