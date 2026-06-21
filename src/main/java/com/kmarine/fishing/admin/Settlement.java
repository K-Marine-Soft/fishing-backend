package com.kmarine.fishing.admin;

import com.kmarine.fishing.vessel.Vessel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;

    @Column(nullable = false)
    private LocalDate periodStart;      // 정산 시작일
    @Column(nullable = false)
    private LocalDate periodEnd;        // 정산 종료일

    private Integer totalRevenue;       // 총 매출
    private Integer platformFee;        // 플랫폼 수수료 (%)
    private Integer platformFeeAmount;  // 수수료 금액
    private Integer settleAmount;       // 정산 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    private LocalDateTime settledAt;    // 정산 완료 일시
    private String memo;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static Settlement create(Vessel vessel,
                                     LocalDate start, LocalDate end,
                                     Integer totalRevenue, Integer feeRate) {
        Settlement s = new Settlement();
        s.vessel             = vessel;
        s.periodStart        = start;
        s.periodEnd          = end;
        s.totalRevenue       = totalRevenue;
        s.platformFee        = feeRate;
        s.platformFeeAmount  = totalRevenue * feeRate / 100;
        s.settleAmount       = totalRevenue - s.platformFeeAmount;
        s.status             = SettlementStatus.PENDING;
        return s;
    }

    // 정산 완료
    public void complete(String memo) {
        this.status     = SettlementStatus.COMPLETED;
        this.settledAt  = LocalDateTime.now();
        this.memo       = memo;
    }
}