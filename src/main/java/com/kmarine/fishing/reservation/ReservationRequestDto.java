package com.kmarine.fishing.reservation;

import jakarta.validation.constraints.*;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

public class ReservationRequestDto {

    @Getter
    public static class Create {
        @NotNull(message = "선박 ID를 입력해주세요")
        private Long vesselId;

        @NotNull(message = "예약 날짜를 입력해주세요")
        @Future(message = "예약 날짜는 오늘 이후여야 합니다")
        private LocalDate reservationDate;

        @NotNull(message = "승선 인원을 입력해주세요")
        @Min(value = 1, message = "최소 1명 이상이어야 합니다")
        private Integer passengerCount;

        private List<MemberInfo> members;   // 탑승자 명단 (선택)

        private List<Integer> seatNumbers;  // 선택 좌석 번호 (선택)

        private String depositorName;       // 입금자명 (예약자와 다를 경우, 선택)

        private String requestMemo;         // 예약시 전하실 말씀 (선택)
    }

    @Getter
    public static class MemberInfo {
        private String name;
        private String phone;
        private String idNumber;
    }

    @Getter
    public static class Cancel {
        @NotBlank(message = "취소 사유를 입력해주세요")
        private String cancelReason;
    }
}