package com.kmarine.fishing.admin;

import com.kmarine.fishing.fleet.Fleet;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_id", nullable = false)
    private Fleet fleet;

    @Column(nullable = false)
    private LocalDate periodStart;      // 정산 시작일

    @Column(nullable = false)
    private LocalDate periodEnd;        // 정산 종료일

    private Integer totalRevenue;       // 총 매출
    private Integer monthlyFee;         // 월정액
    private Integer commissionAmount;   // 수수료 금액
    private Integer platformFeeAmount;  // 플랫폼 수수료 합계
    private Integer settleAmount;       // 최종 정산금액

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;    // PENDING / COMPLETED

    private String memo;
    private LocalDate settledAt;        // 정산 완료일

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Settlement create(
            Fleet fleet,
            LocalDate start, LocalDate end,
            int totalRevenue,
            int monthlyFee,
            int commissionRate) {
        Settlement s = new Settlement();
        s.fleet            = fleet;
        s.periodStart      = start;
        s.periodEnd        = end;
        s.totalRevenue     = totalRevenue;
        s.monthlyFee       = monthlyFee;
        s.commissionAmount =
            (int)(totalRevenue * commissionRate / 100.0);
        s.platformFeeAmount =
            monthlyFee + s.commissionAmount;
        s.settleAmount     =
            totalRevenue - s.platformFeeAmount;
        s.status           = SettlementStatus.PENDING;
        return s;
    }

    public void complete(String memo) {
        this.status     = SettlementStatus.COMPLETED;
        this.memo       = memo;
        this.settledAt  = LocalDate.now();
    }
}