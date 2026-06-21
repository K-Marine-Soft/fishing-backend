package com.kmarine.fishing.payment;

import com.kmarine.fishing.reservation.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, unique = true)
    private String impUid;          // 포트원 결제 고유번호

    @Column(nullable = false, unique = true)
    private String merchantUid;     // 주문번호 (우리 서버)

    @Column(nullable = false)
    private Integer amount;         // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String payMethod;       // card, kakaopay, naverpay 등
    private String cancelReason;    // 환불 사유
    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static Payment create(Reservation reservation,
                                  String impUid, String merchantUid,
                                  Integer amount, String payMethod) {
        Payment p = new Payment();
        p.reservation  = reservation;
        p.impUid       = impUid;
        p.merchantUid  = merchantUid;
        p.amount       = amount;
        p.payMethod    = payMethod;
        p.status       = PaymentStatus.PAID;
        return p;
    }

    // 환불
    public void cancel(String reason) {
        this.status       = PaymentStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt  = LocalDateTime.now();
    }
}