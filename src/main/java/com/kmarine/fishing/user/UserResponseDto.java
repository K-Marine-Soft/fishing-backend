package com.kmarine.fishing.user;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private String provider;
    private LocalDateTime createdAt;
}