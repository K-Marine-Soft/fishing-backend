package com.kmarine.fishing.payment;

public enum PaymentStatus {
    READY,      // 결제 준비
    PAID,       // 결제 완료
    FAILED,     // 결제 실패
    CANCELLED   // 환불
}