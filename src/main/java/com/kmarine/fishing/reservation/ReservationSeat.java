package com.kmarine.fishing.reservation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation_seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private Integer seatNumber;

    public static ReservationSeat create(Reservation reservation, Integer seatNumber) {
        ReservationSeat s = new ReservationSeat();
        s.reservation = reservation;
        s.seatNumber  = seatNumber;
        return s;
    }
}
