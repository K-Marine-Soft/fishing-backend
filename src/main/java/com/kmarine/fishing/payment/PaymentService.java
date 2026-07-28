package com.kmarine.fishing.payment;

import com.kmarine.fishing.config.FcmService;
import com.kmarine.fishing.reservation.Reservation;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.user.User;

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
    private final FcmService            fcmService;

    // 결제 검증 (프론트 결제 완료 후 호출) — 포트원에서 실 결제정보를 조회해 금액/상태를 검증하고 Payment를 저장
    @Transactional
    public PaymentResponseDto.Info verify(String impUid, Long reservationId) {
        if (paymentRepository.findByImpUid(impUid).isPresent()) {
            throw new IllegalArgumentException("이미 검증된 결제입니다.");
        }

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() ->
                    new IllegalArgumentException("예약을 찾을 수 없습니다."));

        Map paymentInfo = portOneService.getPaymentInfo(impUid);
        Integer paidAmount   = (Integer) paymentInfo.get("amount");
        String  status       = (String) paymentInfo.get("status");
        String  merchantUid  = (String) paymentInfo.get("merchant_uid");
        String  payMethod    = (String) paymentInfo.get("pay_method");

        log.info("결제 조회: impUid={} amount={} status={}",
                 impUid, paidAmount, status);

        if (paidAmount == null || !paidAmount.equals(reservation.getTotalPrice())) {
            throw new IllegalArgumentException(
                "결제 금액 불일치: " + paidAmount + " != " + reservation.getTotalPrice());
        }

        if (!"paid".equals(status)) {
            throw new IllegalArgumentException("결제 미완료: " + status);
        }

        Payment payment = Payment.create(
                reservation, impUid, merchantUid, paidAmount, payMethod);
        paymentRepository.save(payment);

        reservation.confirm();

        if (reservation.getUser().getFcmToken() != null) {
            fcmService.sendToToken(
                reservation.getUser().getFcmToken(),
                "예약 확정 ✅",
                reservation.getVessel().getName() +
                " 예약이 확정됐습니다!"
            );
        }

        User captain = reservation.getVessel().getOwner();
        if (captain.getFcmToken() != null) {
            fcmService.sendToToken(
                captain.getFcmToken(),
                "새 예약 알림 🚢",
                reservation.getReservationDate() + " " +
                reservation.getPassengerCount() + "명 예약 들어왔습니다!"
            );
        }

        log.info("결제 검증 완료: impUid={}", impUid);
        return toInfo(payment);
    }

    // 환불
    @Transactional
    public void cancel(Long userId, PaymentRequestDto.Cancel request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (!payment.getReservation().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 환불된 결제입니다.");
        }

        portOneService.cancelPayment(payment.getImpUid(), request.getReason());

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
