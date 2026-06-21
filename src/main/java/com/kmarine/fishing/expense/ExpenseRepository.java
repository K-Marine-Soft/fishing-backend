package com.kmarine.fishing.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<VesselExpense, Long> {

    // 선박별 연도/월 조회
    List<VesselExpense> findByVesselIdAndExpenseYearAndExpenseMonth(
            Long vesselId, Integer year, Integer month);

    // 선박별 연도 전체 조회
    List<VesselExpense> findByVesselIdAndExpenseYear(
            Long vesselId, Integer year);

    // 연도별 항목별 합계 (전년/전전년 비교용)
    @Query("""
        SELECT e.category, e.expenseYear, SUM(e.amount)
        FROM VesselExpense e
        WHERE e.vessel.id = :vesselId
        AND e.expenseYear IN (:years)
        GROUP BY e.category, e.expenseYear
        ORDER BY e.category, e.expenseYear
        """)
    List<Object[]> sumByCategoryAndYear(
            @Param("vesselId") Long vesselId,
            @Param("years") List<Integer> years);

    // 월별 합계
    @Query("""
        SELECT e.expenseYear, e.expenseMonth, SUM(e.amount)
        FROM VesselExpense e
        WHERE e.vessel.id = :vesselId
        AND e.expenseYear = :year
        GROUP BY e.expenseYear, e.expenseMonth
        ORDER BY e.expenseMonth
        """)
    List<Object[]> sumByMonth(
            @Param("vesselId") Long vesselId,
            @Param("year") Integer year);
}