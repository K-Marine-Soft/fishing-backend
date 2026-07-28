package com.kmarine.fishing.admin;

import lombok.Builder;
import lombok.Getter;

public class AdminResponseDto {

    // 대시보드
    @Getter
    @Builder
    public static class Dashboard {
        private Long totalUsers;        // 전체 회원수
        private Long totalVessels;      // 전체 선박수
        private Long pendingVessels;    // 승인 대기 선박
        private Long totalReservations; // 전체 예약수
        private Integer totalRevenue;   // 총 매출
        private Long pendingSettlements;// 정산 대기
    }

}