package com.kmarine.fishing.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneService {

    @Value("${portone.imp-key}")
    private String impKey;

    @Value("${portone.imp-secret}")
    private String impSecret;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.iamport.kr")
            .build();

    // 액세스 토큰 발급
    private String getAccessToken() {
        Map<String, String> body = Map.of(
                "imp_key", impKey,
                "imp_secret", impSecret
        );

        Map response = webClient.post()
                .uri("/users/getToken")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map responseMap = (Map) response.get("response");
        return (String) responseMap.get("access_token");
    }

    // 결제 정보 조회
    public Map getPaymentInfo(String impUid) {
        String token = getAccessToken();

        Map response = webClient.get()
                .uri("/payments/" + impUid)
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (Map) response.get("response");
    }

    // 결제 취소 (환불)
    public void cancelPayment(String impUid, String reason) {
        String token = getAccessToken();

        Map<String, String> body = Map.of(
                "imp_uid", impUid,
                "reason", reason
        );

        webClient.post()
                .uri("/payments/cancel")
                .header("Authorization", token)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        log.info("환불 완료 impUid: {}", impUid);
    }
}