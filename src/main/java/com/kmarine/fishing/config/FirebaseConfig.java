package com.kmarine.fishing.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        log.info("===== FirebaseConfig init() 호출됨 =====");

        try {
            // 이미 초기화된 경우 스킵
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase 이미 초기화됨");
                return;
            }

            ClassPathResource resource =
                    new ClassPathResource(
                        "firebase-service-account.json");

            // 파일 없으면 스킵
            if (!resource.exists()) {
                log.warn("firebase-service-account.json" +
                         " 파일 없음 → FCM 비활성화");
                return;
            }

            FirebaseOptions options = FirebaseOptions
                    .builder()
                    .setCredentials(
                        GoogleCredentials.fromStream(
                            resource.getInputStream()))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("===== Firebase 초기화 완료 =====");

        } catch (IOException e) {
            log.warn("Firebase 초기화 실패 (무시): {}",
                     e.getMessage());
        } catch (Exception e) {
            log.warn("Firebase 예외 발생 (무시): {}",
                     e.getMessage());
        }
    }
}
/**package com.kmarine.fishing.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
    	log.info("===== FirebaseConfig init() 호출됨 =====");
        try {
            ClassPathResource resource = new ClassPathResource(
                    "firebase-service-account.json");

            // 파일 없으면 스킵
            if (!resource.exists()) {
                log.warn("Firebase 서비스 계정 파일 없음 → FCM 비활성화");
                return;
            }
            
            if (FirebaseApp.getApps().isEmpty()) {
               // ClassPathResource resource = new ClassPathResource(
                //        "firebase-service-account.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials
                                .fromStream(resource.getInputStream()))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase 초기화 완료");
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 실패: {}", e.getMessage());
        }
    }
}**/