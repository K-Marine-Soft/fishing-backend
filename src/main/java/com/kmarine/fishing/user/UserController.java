package com.kmarine.fishing.user;

import com.kmarine.fishing.common.ApiResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMe(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.getMe(userId)));
    }

    // FCM 토큰 저장
    @PutMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> body) {
        userService.updateFcmToken(userId, body.get("fcmToken"));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}