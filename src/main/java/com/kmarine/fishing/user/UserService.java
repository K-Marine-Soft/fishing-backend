package com.kmarine.fishing.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 내 정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // 선주 신청
    @Transactional
    public void applyForCaptain(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getRole() == UserRole.ROLE_CAPTAIN) {
            throw new IllegalArgumentException("이미 선주입니다.");
        }
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException(
                "관리자는 선주 신청이 불가합니다.");
        }

        user.updateRole(UserRole.ROLE_CAPTAIN);
        log.info("선주 신청 완료 userId: {}", userId);
    }
    // FCM 토큰 저장
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다."));
        user.updateFcmToken(fcmToken);
    }
}