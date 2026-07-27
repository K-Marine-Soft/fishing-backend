package com.kmarine.fishing.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 내 예약 목록 (전체)
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 내 예약 목록 (특정 선단 범위)
    List<Reservation> findByUserIdAndVessel_Fleet_IdOrderByCreatedAtDesc(
            Long userId, Long fleetId);

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

    // 특정 날짜의 대기 목록 (승격 순서 = 신청 순)
    List<Reservation> findByVessel_IdAndReservationDateAndStatusOrderByCreatedAtAsc(
            Long vesselId, LocalDate date, ReservationStatus status);

    // 월간 예약 현황 집계용 — 선박의 해당 기간 전체 예약
    List<Reservation> findByVessel_IdAndReservationDateBetween(
            Long vesselId, LocalDate start, LocalDate end);

    // 출조일이 지난 확정 예약 (출항완료 자동 배치 대상)
    List<Reservation> findByStatusAndReservationDateBefore(
            ReservationStatus status, LocalDate date);

    // 특정 선박/날짜의 점유된 좌석 번호 (입금대기·확정 예약 기준)
    @Query("""
        SELECT s.seatNumber
        FROM Reservation r JOIN r.seats s
        WHERE r.vessel.id = :vesselId
        AND r.reservationDate = :date
        AND r.status IN ('PENDING', 'CONFIRMED')
        """)
    List<Integer> findOccupiedSeatNumbers(
            @Param("vesselId") Long vesselId,
            @Param("date") LocalDate date);
}