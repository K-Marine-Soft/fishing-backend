package com.kmarine.fishing.expense;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDate;

public class ExpenseRequestDto {

    @Getter
    public static class Create {
        @NotNull(message = "선박 ID를 입력해주세요")
        private Long vesselId;

        @NotNull(message = "날짜를 입력해주세요")
        private LocalDate expenseDate;

        @NotNull(message = "경비 항목을 선택해주세요")
        private ExpenseCategory category;

        @NotNull(message = "금액을 입력해주세요")
        @Min(value = 0, message = "금액은 0원 이상이어야 합니다")
        private Integer amount;

        private String memo;
    }

    // 조회 조건
    @Getter
    public static class Search {
        private Long vesselId;
        private Integer year;
        private Integer month;
        private ExpenseCategory category;
    }
}