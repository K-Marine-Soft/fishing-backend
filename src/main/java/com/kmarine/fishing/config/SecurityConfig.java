package com.kmarine.fishing.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kmarine.fishing.auth.OAuth2SuccessHandler;
import com.kmarine.fishing.auth.OAuth2UserService;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ← 추가
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 보안 헤더 추가
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "img-src 'self' https: data:; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline';"))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss
                    .headerValue(org.springframework.security
                        .web.header.writers.XXssProtectionHeaderWriter
                        .HeaderValue.ENABLED_MODE_BLOCK))
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))           
            .authorizeHttpRequests(auth -> auth
            	.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ← 추가
                // 인증 없이 접근 가능
                .requestMatchers(HttpMethod.GET,
                    "/api/vessels/search",
                    "/api/vessels/{id}",
                    "/api/vessels/{id}/available-dates",
                    "/api/vessels/{id}/reservation-status",
                    "/api/vessels/reservation-status",
                    "/api/vessels/{id}/seats",
                    "/api/fleets",
                    "/api/fleets/subdomain/{subdomain}",
                    "/api/fleets/domain",
                    "/uploads/**",
                    "/api/files/download/**"
                ).permitAll()
                .requestMatchers(
               		"/ws/**",       // ← 추가
                    "/api/health",
                    "/api/auth/**",
                    "/api/posts",           // ← 추가
                    "/api/posts/search",    // ← 추가
                    "/api/posts/{id}" ,      // ← 추가
                    "/api/posts/**",       // ← 추가
                    "/api/reviews/vessel/{vesselId}",  // ← 추가
                    "/api/ocean/**"  // ← 추가
                ).permitAll()
                // 선단 관리자만 접근
                .requestMatchers("/api/captain/**").hasRole("FLEET_ADMIN")
                // 관리자만 접근
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/me").authenticated()
                // 나머지는 로그인 필요
                .anyRequest().authenticated()
            )
            // OAuth2 로그인 설정 추가
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            )            
            .addFilterBefore(
                new JwtFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
    // CORS 설정 통합
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:19006");
        // 와일드카드 서브도메인은 addAllowedOrigin(완전일치)이 아니라
        // addAllowedOriginPattern으로 등록해야 실제로 매칭됩니다.
        config.addAllowedOriginPattern("http://*.kmarine.com:5173");
        config.addAllowedOriginPattern("http://*.kmarine.com:3000");
        config.addAllowedOrigin("http://kmarine.com:5173");     // ← 추가
        config.addAllowedOrigin("https://xxx.vercel.app"); // ← 운영 추가
        config.addAllowedOrigin("https://fishing-web.vercel.app"); // ← 추가
        config.addAllowedOrigin("https://fishing-web-xxxx.vercel.app");
        config.addAllowedOrigin("https://kmarine.com");            // ← 추가
        config.addAllowedOriginPattern("https://*.kmarine.com");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        //source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/**", config);
        source.registerCorsConfiguration(
                "/login/oauth2/**", config);        
        return source;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

//package com.kmarine.fishing.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll()  // 임시 — 나중에 변경
//            );
//        return http.build();
//    }
//}