package com.kmarine.fishing.dailylog;

import com.kmarine.fishing.expense.ExpenseCategory;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DailyLogResponseDto {

    @Getter
    @Builder
    public static class Summary {
        private Long id;
        private LocalDate logDate;
        private LocalDateTime departureTime;
        private LocalDateTime returnTime;
        private Integer totalPassengers;
        private Integer locationCount;  // 이동 횟수
        private Integer totalFishCount; // 총 마릿수
        private SyncStatus syncStatus;
    }

    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private LocalDate logDate;
        private LocalDateTime departureTime;
        private LocalDateTime returnTime;
        private Integer totalPassengers;
        private String memo;
        private SyncStatus syncStatus;
        private List<LocationInfo> locations;
        private List<ExpenseInfo> expenses;
    }

    @Getter
    @Builder
    public static class LocationInfo {
        private Long id;
        private Integer sequence;
        private LocalDateTime recordedAt;
        private Double latitude;
        private Double longitude;
        private String locationName;
        private String tideType;
        private Integer tideCycle;
        private String weatherCondition;
        private Double waterTemp;
        private List<FishInfo> fishRecords;
    }

    @Getter
    @Builder
    public static class FishInfo {
        private String fishType;
        private Integer count;
        private Double avgSize;
        private String method;
    }

    @Getter
    @Builder
    public static class ExpenseInfo {
        private ExpenseCategory category;
        private Integer amount;
        private String memo;
    }

    // 조황 분석 — 물때별 평균 마릿수
    @Getter
    @Builder
    public static class TideAnalysis {
        private Integer tideCycle;
        private String tideType;
        private Double avgFishCount;
        private Integer totalCount;
        private Integer visitCount;
    }

    // 조황 분석 — 장소별 누적
    @Getter
    @Builder
    public static class LocationAnalysis {
        private String locationName;
        private Double latitude;
        private Double longitude;
        private Integer totalFishCount;
        private Integer visitCount;
        private String bestTide;
    }
}