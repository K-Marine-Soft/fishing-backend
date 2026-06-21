package com.kmarine.fishing.payment;

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