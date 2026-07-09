package com.kmarine.fishing.admin;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettlementResponseDto {

    @Getter
    @Builder
    public static class Info {
        private Long            id;
        private Long            fleetId;
        private String          fleetName;
        private String          ownerName;
        private LocalDate       periodStart;
        private LocalDate       periodEnd;
        private Integer         totalRevenue;
        private Integer         monthlyFee;
        private Integer         commissionAmount;
        private Integer         platformFeeAmount;
        private Integer         settleAmount;
        private SettlementStatus status;
        private String          memo;
        private LocalDate       settledAt;
        private LocalDateTime   createdAt;
    }

    // 선단별 정산 요약
    @Getter
    @Builder
    public static class Summary {
        private Long    fleetId;
        private String  fleetName;
        private Integer totalRevenue;       // 총 매출
        private Integer totalPlatformFee;   // 총 플랫폼 수수료
        private Integer totalSettleAmount;  // 총 정산금액
        private Long    pendingCount;       // 미정산 건수
        private Long    completedCount;     // 완료 건수
    }
}