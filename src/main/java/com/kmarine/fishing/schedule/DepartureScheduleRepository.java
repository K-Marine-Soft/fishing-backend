package com.kmarine.fishing.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DepartureScheduleRepository
        extends JpaRepository<DepartureSchedule, Long> {

    Optional<DepartureSchedule> findByVesselIdAndScheduleDate(
            Long vesselId, LocalDate scheduleDate);

    List<DepartureSchedule> findByVesselIdAndScheduleDateBetween(
            Long vesselId, LocalDate start, LocalDate end);
}
