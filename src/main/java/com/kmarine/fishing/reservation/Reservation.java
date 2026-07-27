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

    private String depositorName;       // 입금자명 (예약자와 다를 경우)

    private String requestMemo;         // 예약시 전하실 말씀

    @OneToMany(mappedBy = "reservation",
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "reservation",
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationSeat> seats = new ArrayList<>();

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
        r.depositorName   = request.getDepositorName();
        r.requestMemo     = request.getRequestMemo();
        return r;
    }

    // 좌석 배정
    public void assignSeats(List<Integer> seatNumbers) {
        seatNumbers.forEach(n -> this.seats.add(ReservationSeat.create(this, n)));
    }

    // 예약 확정 (결제 완료 후)
    public void confirm() { this.status = ReservationStatus.CONFIRMED; }

    // 대기 등록
    public void waitlist() { this.status = ReservationStatus.WAITLISTED; }

    // 대기 → 입금 대기로 승격 (자리가 났을 때)
    public void promote() { this.status = ReservationStatus.PENDING; }

    // 취소
    public void cancel(String reason) {
        this.status       = ReservationStatus.CANCELLED;
        this.cancelReason = reason;
    }

    // 완료
    public void complete() { this.status = ReservationStatus.COMPLETED; }
}