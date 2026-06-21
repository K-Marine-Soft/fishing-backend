package com.kmarine.fishing.auth;

import com.kmarine.fishing.config.JwtUtil;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JwtUtil           jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // 회원가입
    @Transactional
    public AuthResponseDto.TokenInfo signUp(AuthRequestDto.SignUp request) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 생성
        User user = User.createLocal(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getPhone()
        );
        userRepository.save(user);

        return generateTokenInfo(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public AuthResponseDto.TokenInfo login(AuthRequestDto.Login request) {

        // 이메일 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return generateTokenInfo(user);
    }

    // 토큰 재발급
    public AuthResponseDto.TokenInfo refresh(AuthRequestDto.Refresh request) {

        String refreshToken = request.getRefreshToken();

        // 유효성 검사
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 타입 확인
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new IllegalArgumentException("Refresh Token이 아닙니다.");
        }

        Long userId = jwtUtil.getUserId(refreshToken);

        // Redis에서 저장된 토큰과 비교
        String savedToken = redisTemplate.opsForValue().get("refresh:" + userId);
        if (!refreshToken.equals(savedToken)) {
            throw new IllegalArgumentException("토큰이 일치하지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return generateTokenInfo(user);
    }

    // 로그아웃
    public void logout(Long userId) {
        redisTemplate.delete("refresh:" + userId);
        log.info("로그아웃 완료 userId: {}", userId);
    }

    // 토큰 생성 공통 메서드
    private AuthResponseDto.TokenInfo generateTokenInfo(User user) {

        String accessToken  = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Refresh Token Redis 저장 (7일)
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                7, TimeUnit.DAYS
        );

        return AuthResponseDto.TokenInfo.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}