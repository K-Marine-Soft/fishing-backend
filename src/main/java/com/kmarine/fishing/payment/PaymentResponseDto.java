package com.kmarine.fishing.payment;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

public class PaymentResponseDto {

    @Getter
    @Builder
    public static class Info {
        private Long id;
        private String impUid;
        private String merchantUid;
        private Integer amount;
        private PaymentStatus status;
        private String payMethod;
        private LocalDateTime createdAt;
    }
}