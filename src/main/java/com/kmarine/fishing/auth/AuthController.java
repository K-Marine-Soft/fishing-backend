package com.kmarine.fishing.auth;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponseDto.TokenInfo>> signUp(
            @Valid @RequestBody AuthRequestDto.SignUp request) {
        return ResponseEntity.ok(
                ApiResponse.ok(authService.signUp(request)));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto.TokenInfo>> login(
            @Valid @RequestBody AuthRequestDto.Login request) {
        return ResponseEntity.ok(
                ApiResponse.ok(authService.login(request)));
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto.TokenInfo>> refresh(
            @Valid @RequestBody AuthRequestDto.Refresh request) {
        return ResponseEntity.ok(
                ApiResponse.ok(authService.refresh(request)));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}