package com.kmarine.fishing.payment;

import com.kmarine.fishing.config.FcmService;
import com.kmarine.fishing.reservation.Reservation;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.reservation.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository     paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PortOneService        portOneService;

    private final FcmService fcmService;

    // 결제 검증
    @Transactional
    public PaymentResponseDto.Info verify(Long userId,
                                          PaymentRequestDto.Verify request) {

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
    }

    // 환불
    @Transactional
    public void cancel(Long userId, PaymentRequestDto.Cancel request) {

        Payment payment = paymentRepository.findById(request.getPaymentId())
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
                .id(p.getId())
                .impUid(p.getImpUid())
                .merchantUid(p.getMerchantUid())
                .amount(p.getAmount())
                .status(p.getStatus())
                .payMethod(p.getPayMethod())
                .createdAt(p.getCreatedAt())
                .build();
    }
}