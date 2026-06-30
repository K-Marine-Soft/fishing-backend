package com.kmarine.fishing.vessel;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class VesselScheduleResponseDto {

    @Getter
    @Builder
    public static class ScheduleInfo {
        private Long id;
        private LocalDate scheduleDate;
        private ScheduleType type;
        private String memo;
        private Integer reservedCount;  // 예약 인원
        private Integer maxPassengers;  // 최대 인원
    }

    @Getter
    @Builder
    public static class MonthlySchedule {
        private Integer year;
        private Integer month;
        private List<ScheduleInfo> schedules;
        private Map<String, Object> summary; // 통계
    }
}