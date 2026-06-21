package com.kmarine.fishing.expense;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // 경비 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponseDto.Info>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ExpenseRequestDto.Create request) {
        return ResponseEntity.ok(
                ApiResponse.ok(expenseService.create(userId, request)));
    }

    // 월별 경비 조회
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<ExpenseResponseDto.MonthlySummary>> monthly(
            @RequestParam Long vesselId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return ResponseEntity.ok(
                ApiResponse.ok(expenseService.getMonthlySummary(vesselId, year, month)));
    }

    // 전년/전전년 비교
    @GetMapping("/yearly-comparison")
    public ResponseEntity<ApiResponse<ExpenseResponseDto.YearlyComparison>> yearlyComparison(
            @RequestParam Long vesselId,
            @RequestParam Integer year) {
        return ResponseEntity.ok(
                ApiResponse.ok(expenseService.getYearlyComparison(vesselId, year)));
    }

    // 매출/이익 분석
    @GetMapping("/sales-summary")
    public ResponseEntity<ApiResponse<ExpenseResponseDto.SalesSummary>> salesSummary(
            @RequestParam Long vesselId,
            @RequestParam String period,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return ResponseEntity.ok(
                ApiResponse.ok(expenseService.getSalesSummary(
                        vesselId, period, year, month)));
    }
}