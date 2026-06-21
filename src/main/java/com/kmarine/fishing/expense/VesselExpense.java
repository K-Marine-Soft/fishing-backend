package com.kmarine.fishing.expense;

import com.kmarine.fishing.vessel.Vessel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vessel_expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class VesselExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;

    @Column(nullable = false)
    private LocalDate expenseDate;      // 경비 발생 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;   // 경비 항목

    @Column(nullable = false)
    private Integer amount;             // 금액

    private String memo;                // 메모

    // 연도/월 (조회 성능용)
    private Integer expenseYear;
    private Integer expenseMonth;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 생성 메서드
    public static VesselExpense create(Vessel vessel,
                                       ExpenseRequestDto.Create request) {
        VesselExpense e = new VesselExpense();
        e.vessel        = vessel;
        e.expenseDate   = request.getExpenseDate();
        e.category      = request.getCategory();
        e.amount        = request.getAmount();
        e.memo          = request.getMemo();
        e.expenseYear   = request.getExpenseDate().getYear();
        e.expenseMonth  = request.getExpenseDate().getMonthValue();
        return e;
    }
}