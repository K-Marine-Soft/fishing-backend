package com.kmarine.fishing.payment;

import com.kmarine.fishing.config.FcmService;
import com.kmarine.fishing.reservation.Reservation;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.reservation.ReservationStatus;
import com.kmarine.fishing.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository     paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PortOneService        portOneService;

    private final FcmService fcmService;

    @Value("${portone.imp-key:test}")
    private String impKey;
    
    @Value("${portone.imp-secret:test}")
    private String impSecret;
    
    //private final WebClient webClient = WebClient.builder().build();

    private final WebClient webClient =
            WebClient.builder()
                    .baseUrl("https://api.iamport.kr")
                    .build();

    // 포트원 액세스 토큰 발급
    private String getAccessToken() {
        JsonNode res = webClient.post()
                .uri("/users/getToken")
                .bodyValue(Map.of(
                    "imp_key",    impKey,
                    "imp_secret", impSecret
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return res.path("response")
                  .path("access_token")
                  .asText();
    }
    
    // 결제 검증
    @Transactional
    public void verify(String impUid, Long reservationId) {
        try {
            // 1. 토큰 발급
            String token = getAccessToken();

            // 2. 결제 정보 조회
            JsonNode paymentRes = webClient.get()
                    .uri("/payments/" + impUid)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode payment = paymentRes.path("response");
            int    paidAmount = payment.path("amount").asInt();
            String status     = payment.path("status").asText();

            log.info("결제 조회: impUid={} amount={} status={}",
                     impUid, paidAmount, status);

            // 3. 예약 조회
            Reservation reservation = reservationRepository
                    .findById(reservationId)
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "예약을 찾을 수 없습니다."));

            // 4. 금액 검증
            if (paidAmount != reservation.getTotalPrice()) {
                throw new IllegalArgumentException(
                    "결제 금액 불일치: " +
                    paidAmount + " != " +
                    reservation.getTotalPrice());
            }

            // 5. 상태 검증
            if (!"paid".equals(status)) {
                throw new IllegalArgumentException(
                    "결제 미완료: " + status);
            }

            // 6. 예약 확정
            reservation.confirm();

            // 7. FCM 알림 — 이용자
            if (reservation.getUser()
                    .getFcmToken() != null) {
                fcmService.sendToToken(
                    reservation.getUser().getFcmToken(),
                    "예약 확정 ✅",
                    reservation.getVessel().getName() +
                    " 예약이 확정됐습니다!"
                );
            }

            // 8. FCM 알림 — 선주
            if (reservation.getVessel()
                    .getOwner().getFcmToken() != null) {
                fcmService.sendToToken(
                    reservation.getVessel()
                        .getOwner().getFcmToken(),
                    "새 예약 알림 🚢",
                    reservation.getReservationDate() +
                    " " +
                    reservation.getPassengerCount() +
                    "명 예약 들어왔습니다!"
                );
            }

            log.info("결제 검증 완료: impUid={}", impUid);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("결제 검증 실패: {}", e.getMessage());
            throw new IllegalArgumentException(
                "결제 검증 실패: " + e.getMessage());
        }
    }
 

    @Transactional
    public void verify123(String impUid, Long reservationId) {

        try {
            // 1. 포트원 액세스 토큰 발급
            JsonNode tokenRes = webClient.post()
                    .uri("https://api.iamport.kr/users/getToken")
                    .bodyValue(Map.of(
                        "imp_key",    impKey,
                        "imp_secret", impSecret
                    ))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String accessToken = tokenRes
                    .path("response")
                    .path("access_token")
                    .asText();

            // 2. 결제 정보 조회
            JsonNode paymentRes = webClient.get()
                    .uri("https://api.iamport.kr/payments/"
                         + impUid)
                    .header("Authorization", accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            int paidAmount = paymentRes
                    .path("response")
                    .path("amount")
                    .asInt();

            String status = paymentRes
                    .path("response")
                    .path("status")
                    .asText();

            // 3. 예약 조회
            Reservation reservation = reservationRepository
                    .findById(reservationId)
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "예약을 찾을 수 없습니다."));

            // 4. 금액 검증
            if (paidAmount != reservation.getTotalPrice()) {
                throw new IllegalArgumentException(
                    "결제 금액 불일치");
            }

            // 5. 결제 상태 검증
            if (!"paid".equals(status)) {
                throw new IllegalArgumentException(
                    "결제 미완료 상태: " + status);
            }

            // 6. 예약 확정
            reservation.confirm();

            // 7. FCM 알림
            if (reservation.getUser().getFcmToken() != null) {
                fcmService.sendToToken(
                    reservation.getUser().getFcmToken(),
                    "예약 확정 ✅",
                    reservation.getVessel().getName() +
                    " 예약이 확정됐습니다!");
            }

            log.info("결제 검증 완료: impUid={} amount={}",
                     impUid, paidAmount);
            // 선주에게도
            User captain = reservation.getVessel().getOwner();
            if (captain.getFcmToken() != null) {
                fcmService.sendToToken(
                    captain.getFcmToken(),
                    "새 예약 알림 🚢",
                    reservation.getReservationDate() +
                    " " + reservation.getPassengerCount() +
                    "명 예약이 들어왔습니다!"
                );
            }
        } catch (Exception e) {
            log.error("결제 검증 실패: {}", e.getMessage());
            throw new IllegalArgumentException(
                "결제 검증 실패: " + e.getMessage());
        }
    }
    
    // 환불
    @Transactional
    public void cancel(Long userId, PaymentRequestDto.Cancel request) {

        com.kmarine.fishing.payment.Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 본인 확인
        if (!payment.getReservation().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 환불된 결제입니다.");
        }

        // 포트원 환불 요청
        portOneService.cancelPayment(payment.getImpUid(), request.getReason());

        // 상태 업데이트
        payment.cancel(request.getReason());
        payment.getReservation().cancel(request.getReason());

        log.info("환불 완료 paymentId: {}", payment.getId());
        
        
    }

    private PaymentResponseDto.Info toInfo(Payment p) {
        return PaymentResponseDto.Info.builder()
                //.id(p.getId())
                .impUid(p.getImpUid())
                .merchantUid(p.getMerchantUid())
                //.amount(p.getAmount())
                //.status(p.getStatus())
                .payMethod(p.getPayMethod())
                //.createdAt(p.getCreatedAt())
                .build();
    }    
//    @Transactional
//    public void verifyxxxx(String impUid, Long reservationId) throws IamportResponseException {
//        try {
//        	
//        	
//        	
//        	
//        	
//            // 포트원 API로 결제 검증
//            IamportClient client = new IamportClient(
//                    impKey, impSecret);
//            IamportResponse<Payment> response =
//                    client.paymentByImpUid(impUid);
//
//            Payment payment = response.getResponse();
//
//            Reservation reservation = reservationRepository
//                    .findById(reservationId)
//                    .orElseThrow(() ->
//                        new IllegalArgumentException(
//                            "예약을 찾을 수 없습니다."));
//
//            // 금액 검증
//            if (payment.getAmount().intValue() !=
//                    reservation.getTotalPrice()) {
//                throw new IllegalArgumentException(
//                    "결제 금액 불일치");
//            }
//
//            // 예약 확정
//            reservation.confirm();
//
//            //Reservation reservation,
//            //String impUid, String merchantUid,
//            //Integer amount, String payMethod/
//            // 결제 저장
//            com.kmarine.fishing.payment.Payment p =
//                    com.kmarine.fishing.payment.Payment.create(
//                            reservation, impUid,"",
//                            payment.getAmount().intValue(),"");
//            paymentRepository.save(p);
//
//            // FCM 알림
//            if (reservation.getUser().getFcmToken() != null) {
//                fcmService.sendToToken(
//                    reservation.getUser().getFcmToken(),
//                    "예약 확정 ✅",
//                    reservation.getVessel().getName() +
//                    " 예약이 확정됐습니다!");
//            }
//
//        } catch ( IamportResponseException  | IOException e) {
//            throw new IllegalArgumentException(
//                "결제 검증 실패: " + e.getMessage());
//        }
//    }
    /**
    // 결제 검증
    @Transactional
    public PaymentResponseDto.Info verify(String impUid, Long reservationId) {
    	//(Long userId, PaymentRequestDto.Verify request) {

        // 예약 조회
        Reservation reservation = reservationRepository
                .findById(request.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 본인 예약 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 포트원에서 실제 결제 정보 조회
        Map paymentInfo = portOneService.getPaymentInfo(request.getImpUid());

        // 금액 검증 (위변조 방지)
        Integer paidAmount = (Integer) paymentInfo.get("amount");
        if (!paidAmount.equals(reservation.getTotalPrice())) {
            throw new IllegalArgumentException(
                "결제 금액이 일치하지 않습니다. 예약금액: "
                + reservation.getTotalPrice() + " 결제금액: " + paidAmount);
        }

        // 결제 상태 확인
        String status = (String) paymentInfo.get("status");
        if (!"paid".equals(status)) {
            throw new IllegalArgumentException("결제가 완료되지 않았습니다.");
        }

        // Payment 저장
        Payment payment = Payment.create(
                reservation,
                request.getImpUid(),
                (String) paymentInfo.get("merchant_uid"),
                paidAmount,
                (String) paymentInfo.get("pay_method")
        );
        paymentRepository.save(payment);

        // 예약 확정
        reservation.confirm();

        log.info("결제 완료 reservationId: {}, impUid: {}",
                reservation.getId(), request.getImpUid());

     // 결제 검증 메서드 내 예약 확정 후 추가
        reservation.confirm();

        // 이용자에게 알림
        if (reservation.getUser().getFcmToken() != null) {
            fcmService.sendToToken(
                reservation.getUser().getFcmToken(),
                "예약 확정 ✅",
                reservation.getVessel().getName() +
                " " + reservation.getReservationDate() +
                " 예약이 확정됐습니다!"
            );
        }

        // 선주에게 알림
        if (reservation.getVessel().getOwner()
                .getFcmToken() != null) {
            fcmService.sendToToken(
                reservation.getVessel().getOwner().getFcmToken(),
                "새 예약 알림 🚢",
                reservation.getReservationDate() +
                " " + reservation.getPassengerCount() +
                "명 예약이 들어왔습니다!"
            );
        }
        return toInfo(payment);
    }**/


}