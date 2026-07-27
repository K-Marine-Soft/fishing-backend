package com.kmarine.fishing.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    // 단일 기기 알림
    public void sendToToken(String token,
                             String title,
                             String body) {
        // Firebase 미설정 시 스킵
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FCM 미설정 → 알림 스킵");
            return;
        }
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging
                    .getInstance().send(message);
            log.info("FCM 전송 완료: {}", response);
        } catch (Exception e) {
            log.error("FCM 전송 실패: {}", e.getMessage());
        }
    }

    // 토픽 알림 (다수 기기)
    public void sendToTopic(String topic,
                             String title,
                             String body) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging
                    .getInstance().send(message);
            log.info("FCM 토픽 전송 완료: {}", response);
        } catch (Exception e) {
            log.error("FCM 토픽 전송 실패: {}", e.getMessage());
        }
    }
}