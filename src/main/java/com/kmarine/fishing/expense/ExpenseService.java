package com.kmarine.fishing.expense;

import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.vessel.VesselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository     expenseRepository;
    private final VesselRepository      vesselRepository;
    private final ReservationRepository reservationRepository;

    // 경비 등록
    @Transactional
    public ExpenseResponseDto.Info create(Long userId,
                                          ExpenseRequestDto.Create request) {
        Vessel vessel = vesselRepository.findById(request.getVesselId())
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));

        // 선주 본인 확인
        if (!vessel.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        VesselExpense expense = VesselExpense.create(vessel, request);
        expenseRepository.save(expense);

        return ExpenseResponseDto.Info.builder()
                .id(expense.getId())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .memo(expense.getMemo())
                .build();
    }

    // 월별 경비 조회
    @Transactional(readOnly = true)
    public ExpenseResponseDto.MonthlySummary getMonthlySummary(
            Long vesselId, Integer year, Integer month) {

        List<VesselExpense> expenses = expenseRepository
                .findByVesselIdAndExpenseYearAndExpenseMonth(vesselId, year, month);

        Integer total = expenses.stream()
                .mapToInt(VesselExpense::getAmount).sum();

        // 항목별 합계
        Map<String, Integer> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().name(),
                        Collectors.summingInt(VesselExpense::getAmount)
                ));

        return ExpenseResponseDto.MonthlySummary.builder()
                .year(year)
                .month(month)
                .totalAmount(total)
                .byCategory(byCategory)
                .build();
    }

    // 전년/전전년 비교
    @Transactional(readOnly = true)
    public ExpenseResponseDto.YearlyComparison getYearlyComparison(
            Long vesselId, Integer currentYear) {

        int lastYear      = currentYear - 1;
        int twoYearsAgo   = currentYear - 2;

        List<Object[]> rows = expenseRepository.sumByCategoryAndYear(
                vesselId, List.of(twoYearsAgo, lastYear, currentYear));

        // 연도별 총합
        Map<Integer, Integer> yearTotal = new HashMap<>();
        yearTotal.put(currentYear, 0);
        yearTotal.put(lastYear, 0);
        yearTotal.put(twoYearsAgo, 0);

        // 항목별 연도 비교
        Map<String, Map<Integer, Integer>> catMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String cat   = row[0].toString();
            Integer year = (Integer) row[1];
            Integer sum  = ((Number) row[2]).intValue();

            catMap.computeIfAbsent(cat, k -> new HashMap<>()).put(year, sum);
            yearTotal.merge(year, sum, Integer::sum);
        }

        List<ExpenseResponseDto.CategoryComparison> categories = catMap.entrySet()
                .stream()
                .map(entry -> ExpenseResponseDto.CategoryComparison.builder()
                        .category(entry.getKey())
                        .currentYear(entry.getValue().getOrDefault(currentYear, 0))
                        .lastYear(entry.getValue().getOrDefault(lastYear, 0))
                        .twoYearsAgo(entry.getValue().getOrDefault(twoYearsAgo, 0))
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponseDto.YearlyComparison.builder()
                .currentYear(currentYear)
                .lastYear(lastYear)
                .twoYearsAgo(twoYearsAgo)
                .currentTotal(yearTotal.get(currentYear))
                .lastTotal(yearTotal.get(lastYear))
                .twoYearsAgoTotal(yearTotal.get(twoYearsAgo))
                .categories(categories)
                .build();
    }

    // 매출/이익 기간별 분석
    @Transactional(readOnly = true)
    public ExpenseResponseDto.SalesSummary getSalesSummary(
            Long vesselId, String period, Integer year, Integer month) {

        // 현재 기간 매출 (확정된 예약 합계)
        Integer currentRevenue = getRevenue(vesselId, year, month, period);
        Integer currentExpense = getExpense(vesselId, year, month, period);
        Integer currentProfit  = currentRevenue - currentExpense;

        // 전기 매출
        Integer[] prevPeriod   = getPrevPeriod(period, year, month);
        Integer prevRevenue    = getRevenue(vesselId, prevPeriod[0], prevPeriod[1], period);
        Integer prevExpense    = getExpense(vesselId, prevPeriod[0], prevPeriod[1], period);
        Integer prevProfit     = prevRevenue - prevExpense;

        // 증감률
        Double revenueGrowth = prevRevenue == 0 ? 0.0
                : Math.round((currentRevenue - prevRevenue) * 100.0 / prevRevenue * 10) / 10.0;
        Double profitGrowth  = prevProfit == 0 ? 0.0
                : Math.round((currentProfit - prevProfit) * 100.0 / prevProfit * 10) / 10.0;

        return ExpenseResponseDto.SalesSummary.builder()
                .period(period)
                .totalRevenue(currentRevenue)
                .totalExpense(currentExpense)
                .totalProfit(currentProfit)
                .prevRevenue(prevRevenue)
                .prevExpense(prevExpense)
                .prevProfit(prevProfit)
                .revenueGrowth(revenueGrowth)
                .profitGrowth(profitGrowth)
                .build();
    }

    // 매출 계산 (예약 확정 건 합계)
    private Integer getRevenue(Long vesselId, Integer year,
                                Integer month, String period) {
        List<com.kmarine.fishing.reservation.Reservation> reservations =
                reservationRepository.findByVesselIdOrderByReservationDateDesc(vesselId);

        return reservations.stream()
                .filter(r -> r.getStatus().name().equals("CONFIRMED"))
                .filter(r -> {
                    LocalDate d = r.getReservationDate();
                    if ("YEARLY".equals(period)) return d.getYear() == year;
                    return d.getYear() == year && d.getMonthValue() == month;
                })
                .mapToInt(com.kmarine.fishing.reservation.Reservation::getTotalPrice)
                .sum();
    }

    // 경비 합계
    private Integer getExpense(Long vesselId, Integer year,
                                Integer month, String period) {
        List<VesselExpense> expenses = "YEARLY".equals(period)
                ? expenseRepository.findByVesselIdAndExpenseYear(vesselId, year)
                : expenseRepository.findByVesselIdAndExpenseYearAndExpenseMonth(
                        vesselId, year, month);

        return expenses.stream().mapToInt(VesselExpense::getAmount).sum();
    }

    // 전기 기간 계산
    private Integer[] getPrevPeriod(String period, Integer year, Integer month) {
        if ("YEARLY".equals(period)) return new Integer[]{year - 1, month};
        if (month == 1)              return new Integer[]{year - 1, 12};
        return new Integer[]{year, month - 1};
    }
}