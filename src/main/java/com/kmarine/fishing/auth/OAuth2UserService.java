package com.kmarine.fishing.auth;

import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService
        extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);

        String provider = request.getClientRegistration()
                .getRegistrationId(); // kakao or naver

        String providerId;
        String email;
        String name;

        if ("kakao".equals(provider)) {
            // 카카오 사용자 정보 파싱
            Map<String, Object> attributes =
                    oAuth2User.getAttributes();
            providerId = String.valueOf(attributes.get("id"));

            Map<String, Object> kakaoAccount =
                    (Map<String, Object>)
                    attributes.get("kakao_account");
            Map<String, Object> profile =
                    (Map<String, Object>)
                    kakaoAccount.get("profile");

            email = (String) kakaoAccount
                    .getOrDefault("email",
                        providerId + "@kakao.com");
            name  = (String) profile
                    .getOrDefault("nickname", "카카오사용자");

        } else {
            // 네이버 사용자 정보 파싱
            Map<String, Object> response =
                    (Map<String, Object>)
                    oAuth2User.getAttributes().get("response");

            providerId = (String) response.get("id");
            email      = (String) response.getOrDefault(
                             "email",
                             providerId + "@naver.com");
            name       = (String) response
                             .getOrDefault("name", "네이버사용자");
        }

        // DB에서 사용자 조회 or 생성
        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // 이메일로 기존 계정 확인
                    return userRepository.findByEmail(email)
                            .orElseGet(() -> {
                                // 신규 사용자 생성
                                User newUser = User.createOAuth(
                                        email, name,
                                        provider, providerId,
                                        null);
                                return userRepository.save(newUser);
                            });
                });

        log.info("OAuth2 로그인 성공: {} ({})",
                 user.getEmail(), provider);

        return new OAuth2UserDetails(user, oAuth2User.getAttributes());
    }
}