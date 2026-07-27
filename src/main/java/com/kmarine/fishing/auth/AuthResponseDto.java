package com.kmarine.fishing.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AuthResponseDto {

    // 로그인/회원가입 응답
    @Getter
    @Builder
    @AllArgsConstructor
    public static class TokenInfo {
        private Long userId;
        private String email;
        private String name;
        private String role;
        private Long fleetId;      // 선단관리자인 경우 소속 선단 id
        private String accessToken;
        private String refreshToken;
    }
}