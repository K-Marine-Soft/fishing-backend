package com.kmarine.fishing.reservation;

public enum ReservationStatus {
    PENDING,     // 입금 대기 (결제 전)
    CONFIRMED,   // 입금 완료
    WAITLISTED,  // 정원 초과로 대기 중 (자리 나면 PENDING으로 승격)
    CANCELLED,   // 취소
    COMPLETED    // 완료 (출항 후)
}