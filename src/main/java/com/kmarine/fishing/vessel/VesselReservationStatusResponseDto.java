package com.kmarine.fishing.vessel;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class VesselReservationStatusResponseDto {
    private LocalDate date;
    private Long vesselId;
    private String vesselName;
    private String scheduleType;   // AVAILABLE / CLOSED / FULL
    private String memo;           // 공지사항
    private String fishType;       // 낚시 종류
    private int maxPassengers;
    private int remainingSeats;    // 예약가능 잔여석

    private List<ReservationEntry> confirmedEntries;   // 입금자
    private List<ReservationEntry> pendingEntries;     // 입금대기
    private List<ReservationEntry> waitlistedEntries;  // 대기자
    private int cancelledCount;                        // 취소완료 (인원)

    @Getter
    @Builder
    public static class ReservationEntry {
        private String maskedName;
        private int passengerCount;
    }
}
