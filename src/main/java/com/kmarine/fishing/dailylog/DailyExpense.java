package com.kmarine.fishing.dailylog;

import com.kmarine.fishing.expense.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "daily_expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_log_id", nullable = false)
    private DailyLog dailyLog;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;   // 경비 항목

    private Integer amount;             // 금액
    private String memo;

    public static DailyExpense create(DailyLog dailyLog,
                                       DailyLogRequestDto.ExpenseInfo request) {
        DailyExpense e = new DailyExpense();
        e.dailyLog = dailyLog;
        e.category = request.getCategory();
        e.amount   = request.getAmount();
        e.memo     = request.getMemo();
        return e;
    }
}