package com.kmarine.fishing.reservation;

import com.kmarine.fishing.user.User;
import com.kmarine.fishing.vessel.Vessel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                  // 예약자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;              // 선박

    @Column(nullable = false)
    private LocalDate reservationDate;  // 예약 날짜

    @Column(nullable = false)
    private Integer passengerCount;     // 승선 인원

    @Column(nullable = false)
    private Integer totalPrice;         // 총 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private String cancelReason;        // 취소 사유

    @OneToMany(mappedBy = "reservation",
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationMember> members = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static Reservation create(User user, Vessel vessel,
                                     ReservationRequestDto.Create request) {
        Reservation r = new Reservation();
        r.user            = user;
        r.vessel          = vessel;
        r.reservationDate = request.getReservationDate();
        r.passengerCount  = request.getPassengerCount();
        r.totalPrice      = vessel.getPricePerPerson() * request.getPassengerCount();
        r.status          = ReservationStatus.PENDING;
        return r;
    }

    // 예약 확정 (결제 완료 후)
    public void confirm() { this.status = ReservationStatus.CONFIRMED; }

    // 취소
    public void cancel(String reason) {
        this.status       = ReservationStatus.CANCELLED;
        this.cancelReason = reason;
    }

    // 완료
    public void complete() { this.status = ReservationStatus.COMPLETED; }
}