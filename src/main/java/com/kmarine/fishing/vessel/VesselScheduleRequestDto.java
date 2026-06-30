package com.kmarine.fishing.vessel;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

public class VesselScheduleRequestDto {

    // 단일 날짜 설정
    @Getter
    public static class SetSchedule {
        @NotNull
        private Long vesselId;

        @NotNull
        private LocalDate scheduleDate;

        @NotNull
        private ScheduleType type;

        private String memo;
    }

    // 다중 날짜 설정 (휴항 기간 등)
    @Getter
    public static class SetMultiple {
        @NotNull
        private Long vesselId;

        @NotNull
        private List<LocalDate> dates;

        @NotNull
        private ScheduleType type;

        private String memo;
    }
}