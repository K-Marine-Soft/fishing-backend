package com.kmarine.fishing.schedule;

import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.vessel.VesselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final DepartureScheduleRepository scheduleRepository;
    private final ReservationRepository       reservationRepository;
    private final VesselRepository            vesselRepository;

    // 월별 출항 일정 조회 (예약 인원 + 출항 가능 여부)
    @Transactional(readOnly = true)
    public Map<String, ScheduleResponseDto> getMonthlySchedule(
            Long vesselId, int year, int month) {

        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() ->
                    new IllegalArgumentException("선박을 찾을 수 없습니다."));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        Map<LocalDate, DepartureSchedule> scheduleMap =
            scheduleRepository
                .findByVesselIdAndScheduleDateBetween(
                    vessel.getId(), start, end)
                .stream()
                .collect(Collectors.toMap(
                    DepartureSchedule::getScheduleDate,
                    Function.identity()));

        Map<String, ScheduleResponseDto> result = new HashMap<>();

        start.datesUntil(end.plusDays(1)).forEach(date -> {
            Integer reserved = reservationRepository
                    .sumPassengersByVesselAndDate(
                        vessel.getId(), date);

            DepartureSchedule schedule = scheduleMap.get(date);
            boolean available = schedule == null || schedule.isAvailable();

            result.put(date.toString(),
                ScheduleResponseDto.builder()
                    .reserved(reserved)
                    .available(available)
                    .build());
        });

        return result;
    }

    // 선주 — 출항 가능/불가 설정
    @Transactional
    public ScheduleResponseDto updateSchedule(
            Long ownerId, Long vesselId,
            ScheduleRequestDto.Update request) {

        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() ->
                    new IllegalArgumentException("선박을 찾을 수 없습니다."));

        if (!vessel.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        if (request.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                "과거 날짜는 변경할 수 없습니다.");
        }

        // 출항 불가 설정 시 기존 예약 확인
        if (!request.getAvailable()) {
            Integer reserved = reservationRepository
                    .sumPassengersByVesselAndDate(
                        vesselId, request.getDate());
            if (reserved > 0) {
                throw new IllegalArgumentException(
                    "예약이 있는 날짜는 출항 불가로 설정할 수 없습니다. " +
                    "(예약 " + reserved + "명)");
            }
        }

        DepartureSchedule schedule = scheduleRepository
                .findByVesselIdAndScheduleDate(
                    vesselId, request.getDate())
                .orElseGet(() ->
                    DepartureSchedule.create(
                        vessel, request.getDate(),
                        request.getAvailable()));

        schedule.setAvailable(request.getAvailable());

        // 출항 가능으로 되돌릴 때는 기본 상태이므로 레코드 삭제
        if (request.getAvailable()) {
            if (schedule.getId() != null) {
                scheduleRepository.delete(schedule);
            }
        } else {
            scheduleRepository.save(schedule);
        }

        Integer reserved = reservationRepository
                .sumPassengersByVesselAndDate(
                    vesselId, request.getDate());

        return ScheduleResponseDto.builder()
                .reserved(reserved)
                .available(request.getAvailable())
                .build();
    }

    // 예약 생성 시 출항 가능 여부 확인
    @Transactional(readOnly = true)
    public void validateDepartureAvailable(
            Long vesselId, LocalDate date) {

        scheduleRepository
            .findByVesselIdAndScheduleDate(vesselId, date)
            .filter(s -> !s.isAvailable())
            .ifPresent(s -> {
                throw new IllegalArgumentException(
                    "선주가 출항 불가로 설정한 날짜입니다.");
            });
    }
}
