package com.kmarine.fishing.vessel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VesselScheduleRepository
        extends JpaRepository<VesselSchedule, Long> {

    // 선박별 월간 일정
    @Query("""
        SELECT s FROM VesselSchedule s
        WHERE s.vessel.id = :vesselId
        AND s.scheduleDate >= :start
        AND s.scheduleDate <= :end
        ORDER BY s.scheduleDate
        """)
    List<VesselSchedule> findByVesselAndMonth(
            @Param("vesselId") Long vesselId,
            @Param("start")    LocalDate start,
            @Param("end")      LocalDate end);

    // 특정 날짜 일정
    Optional<VesselSchedule> findByVesselIdAndScheduleDate(
            Long vesselId, LocalDate date);
}