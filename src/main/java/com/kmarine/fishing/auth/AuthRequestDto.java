package com.kmarine.fishing.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class AuthRequestDto {

    // 회원가입 요청
    @Getter
    public static class SignUp {
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요")
        //@Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])" +
                         "[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함한 " +
                          "8~20자여야 합니다"
            )        
        private String password;

        @NotBlank(message = "이름을 입력해주세요")
        @Size(max = 20, message = "이름은 20자 이하여야 합니다")
        private String name;

        @Pattern(
                regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다"
            )
        private String phone;
    }

    // 로그인 요청
    @Getter
    public static class Login {
        @NotBlank(message = "이메일을 입력해주세요")
        @Email
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요")
        private String password;
    }

    // 토큰 재발급 요청
    @Getter
    public static class Refresh {
        @NotBlank
        private String refreshToken;
    }
}