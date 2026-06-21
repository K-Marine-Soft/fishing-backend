package com.kmarine.fishing.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDate;

public class AdminRequestDto {

    // 선박 승인/거절
    @Getter
    public static class VesselApprove {
        @NotNull
        private Long vesselId;
        private String memo;
    }

    // 정산 생성
    @Getter
    public static class CreateSettlement {
        @NotNull
        private Long vesselId;

        @NotNull
        private LocalDate periodStart;

        @NotNull
        private LocalDate periodEnd;

        private Integer feeRate = 10;   // 기본 수수료 10%
    }

    // 정산 완료 처리
    @Getter
    public static class CompleteSettlement {
        @NotNull
        private Long settlementId;
        private String memo;
    }
}