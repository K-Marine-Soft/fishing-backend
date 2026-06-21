package com.kmarine.fishing.admin;

import com.kmarine.fishing.vessel.VesselStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // 선박 관리 목록
    @Getter
    @Builder
    public static class VesselInfo {
        private Long id;
        private String name;
        private String ownerName;
        private String ownerEmail;
        private String region;
        private VesselStatus status;
        private LocalDateTime createdAt;
    }

    // 정산 정보
    @Getter
    @Builder
    public static class SettlementInfo {
        private Long id;
        private String vesselName;
        private String ownerName;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private Integer totalRevenue;
        private Integer platformFee;
        private Integer platformFeeAmount;
        private Integer settleAmount;
        private SettlementStatus status;
        private LocalDateTime settledAt;
    }
}