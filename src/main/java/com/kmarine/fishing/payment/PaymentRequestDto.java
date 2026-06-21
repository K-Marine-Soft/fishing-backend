package com.kmarine.fishing.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class PaymentRequestDto {

    // 결제 검증 요청 (프론트에서 결제 완료 후 서버로 전송)
    @Getter
    public static class Verify {
        @NotBlank(message = "imp_uid를 입력해주세요")
        private String impUid;          // 포트원 결제번호

        @NotNull(message = "예약 ID를 입력해주세요")
        private Long reservationId;     // 우리 예약 ID
    }

    // 환불 요청
    @Getter
    public static class Cancel {
        @NotNull
        private Long paymentId;

        @NotBlank(message = "환불 사유를 입력해주세요")
        private String reason;
    }
}