package com.kmarine.fishing.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 내 예약 목록
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 선박별 예약 목록 (선주용)
    List<Reservation> findByVesselIdOrderByReservationDateDesc(Long vesselId);

    
    // 특정 날짜 예약 인원 합계 (중복 예약 방지)
    @Query("""
        SELECT COALESCE(SUM(r.passengerCount), 0)
        FROM Reservation r
        WHERE r.vessel.id = :vesselId
        AND r.reservationDate = :date
        AND r.status IN ('PENDING', 'CONFIRMED')
        """)
    Integer sumPassengersByVesselAndDate(
            @Param("vesselId") Long vesselId,
            @Param("date") LocalDate date);
}