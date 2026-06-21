package com.kmarine.fishing.dailylog;

import com.kmarine.fishing.expense.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DailyLogRequestDto {

    // 일지 생성
    @Getter
    public static class Create {
        @NotNull(message = "선박 ID를 입력해주세요")
        private Long vesselId;

        @NotNull(message = "날짜를 입력해주세요")
        private LocalDate logDate;

        private LocalDateTime departureTime;
        private Integer totalPassengers;
        private String memo;
        private String localId;             // 오프라인 UUID

        private List<AddLocation> locations;    // 위치 목록
        private List<ExpenseInfo> expenses;     // 당일 경비
    }

    // 위치 추가 (이동마다)
    @Getter
    public static class AddLocation {
        @NotNull
        private Long dailyLogId;

        private LocalDateTime recordedAt;
        private Double latitude;
        private Double longitude;
        private String locationName;
        private String tideType;
        private Integer tideCycle;
        private Double currentSpeed;
        private Double waterTemp;
        private String weatherCondition;
        private Double windSpeed;

        private List<FishInfo> fishRecords; // 해당 위치 조황
    }

    // 어종 기록
    @Getter
    public static class FishInfo {
        private String fishType;
        private Integer count;
        private Double avgSize;
        private String method;
    }

    // 경비 정보
    @Getter
    public static class ExpenseInfo {
        private ExpenseCategory category;
        private Integer amount;
        private String memo;
    }

    // 오프라인 동기화 (입항 후 일괄 전송)
    @Getter
    public static class Sync {
        @NotNull
        private Long vesselId;

        private List<Create> dailyLogs; // 미동기화 일지 목록
    }

    // 입항 처리
    @Getter
    public static class Arrive {
        @NotNull
        private Long dailyLogId;

        private LocalDateTime returnTime;
        private List<ExpenseInfo> expenses; // 입항 시 경비 일괄 입력
    }
}