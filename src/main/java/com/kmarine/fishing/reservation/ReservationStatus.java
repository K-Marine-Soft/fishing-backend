package com.kmarine.fishing.reservation;

public enum ReservationStatus {
    PENDING,    // 예약 대기 (결제 전)
    CONFIRMED,  // 예약 확정 (결제 완료)
    CANCELLED,  // 취소
    COMPLETED   // 완료 (출항 후)
}