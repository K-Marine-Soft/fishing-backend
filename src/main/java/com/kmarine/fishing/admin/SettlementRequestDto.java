package com.kmarine.fishing.admin;

import lombok.Getter;
import java.time.LocalDate;

public class SettlementRequestDto {

    @Getter
    public static class Generate {
        private Long      fleetId;
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }

    @Getter
    public static class Complete {
        private String memo;
    }
}