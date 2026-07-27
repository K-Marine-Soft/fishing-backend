package com.kmarine.fishing.reservation;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationResponseDto {

    @Getter
    @Builder
    public static class Summary {
        private Long id;
        private String vesselName;
        private String region;
        private LocalDate reservationDate;
        private Integer passengerCount;
        private Integer totalPrice;
        private ReservationStatus status;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private Long vesselId;
        private String vesselName;
        private String region;
        private String departurePort;
        private LocalDate reservationDate;
        private Integer passengerCount;
        private Integer totalPrice;
        private ReservationStatus status;
        private String cancelReason;
        private List<Integer> seatNumbers;
        private String depositorName;
        private String requestMemo;
        private LocalDateTime createdAt;
    }
}