package com.kmarine.fishing.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;        // 소셜 로그인은 null

    @Column(nullable = false)
    private String name;

    private String phone;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private String provider;        // kakao / naver / google / local
    private String providerId;      // 소셜 고유 ID

    private boolean enabled;        // 계정 활성화 여부

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private String fcmToken;  // FCM 디바이스 토큰
    // FCM 토큰 업데이트
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    // 생성 메서드 — 일반 회원가입
    public static User createLocal(String email, String password,
                                   String name, String phone) {
        User user = new User();
        user.email    = email;
        user.password = password;
        user.name     = name;
        user.phone    = phone;
        user.role     = UserRole.ROLE_USER;
        user.provider = "local";
        user.enabled  = true;
        return user;
    }

    // 생성 메서드 — 소셜 로그인
    public static User createOAuth(String email, String name,
                                   String provider, String providerId,
                                   String profileImage) {
        User user = new User();
        user.email         = email;
        user.name          = name;
        user.provider      = provider;
        user.providerId    = providerId;
        user.profileImage  = profileImage;
        user.role          = UserRole.ROLE_USER;
        user.enabled       = true;
        return user;
    }

    // 비밀번호 변경
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 역할 변경 (선주 승인 시)
    public void updateRole(UserRole role) {
        this.role = role;
    }
}