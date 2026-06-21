package com.kmarine.fishing.expense;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ExpenseResponseDto {

    // 경비 단건
    @Getter
    @Builder
    public static class Info {
        private Long id;
        private LocalDate expenseDate;
        private ExpenseCategory category;
        private Integer amount;
        private String memo;
    }

    // 월별 경비 합계
    @Getter
    @Builder
    public static class MonthlySummary {
        private Integer year;
        private Integer month;
        private Integer totalAmount;
        private Map<String, Integer> byCategory; // 항목별 합계
    }

    // 연도별 비교 (전년/전전년)
    @Getter
    @Builder
    public static class YearlyComparison {
        private Integer currentYear;
        private Integer lastYear;
        private Integer twoYearsAgo;
        private Integer currentTotal;
        private Integer lastTotal;
        private Integer twoYearsAgoTotal;
        private List<CategoryComparison> categories;
    }

    // 항목별 연도 비교
    @Getter
    @Builder
    public static class CategoryComparison {
        private String category;
        private Integer currentYear;
        private Integer lastYear;
        private Integer twoYearsAgo;
    }

    // 매출/이익 일별 요약
    @Getter
    @Builder
    public static class SalesInfo {
        private LocalDate date;
        private Integer revenue;     // 매출
        private Integer expense;     // 경비
        private Integer profit;      // 이익
    }

    // 매출/이익 기간별 비교
    @Getter
    @Builder
    public static class SalesSummary {
        private String period;          // 일/주/월/년
        private Integer totalRevenue;   // 총 매출
        private Integer totalExpense;   // 총 경비
        private Integer totalProfit;    // 총 이익
        private Integer prevRevenue;    // 전기 매출
        private Integer prevExpense;    // 전기 경비
        private Integer prevProfit;     // 전기 이익
        private Double revenueGrowth;   // 매출 증감률 (%)
        private Double profitGrowth;    // 이익 증감률 (%)
    }
}