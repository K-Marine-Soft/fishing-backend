package com.kmarine.fishing.vessel;

import com.kmarine.fishing.vessel.VesselRepository;
import com.kmarine.fishing.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VesselScheduleService {

    private final VesselScheduleRepository scheduleRepository;
    private final VesselRepository         vesselRepository;
    private final ReservationRepository    reservationRepository;

    // 월간 일정 조회
    @Transactional(readOnly = true)
    public VesselScheduleResponseDto.MonthlySchedule
            getMonthlySchedule(Long vesselId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(
                              start.lengthOfMonth());

        List<VesselSchedule> schedules =
                scheduleRepository.findByVesselAndMonth(
                        vesselId, start, end);

        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "선박을 찾을 수 없습니다."));

        List<VesselScheduleResponseDto.ScheduleInfo> infos =
                schedules.stream().map(s -> {
                    Integer reserved = reservationRepository
                            .sumPassengersByVesselAndDate(
                                    vesselId, s.getScheduleDate());
                    return VesselScheduleResponseDto.ScheduleInfo
                            .builder()
                            .id(s.getId())
                            .scheduleDate(s.getScheduleDate())
                            .type(s.getType())
                            .memo(s.getMemo())
                            .reservedCount(reserved)
                            .maxPassengers(vessel.getMaxPassengers())
                            .build();
                }).collect(Collectors.toList());

        // 통계
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDays",     schedules.size());
        summary.put("availableDays", schedules.stream()
                .filter(s -> s.getType() == ScheduleType.AVAILABLE)
                .count());
        summary.put("closedDays", schedules.stream()
                .filter(s -> s.getType() == ScheduleType.CLOSED)
                .count());

        return VesselScheduleResponseDto.MonthlySchedule.builder()
                .year(year)
                .month(month)
                .schedules(infos)
                .summary(summary)
                .build();
    }

    // 일정 설정 (단일)
    @Transactional
    public VesselScheduleResponseDto.ScheduleInfo setSchedule(
            Long captainId,
            VesselScheduleRequestDto.SetSchedule request) {

        Vessel vessel = vesselRepository
                .findById(request.getVesselId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "선박을 찾을 수 없습니다."));

        // 선주 권한 확인
        if (!vessel.getOwner().getId().equals(captainId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 기존 일정 수정 or 신규 생성
        VesselSchedule schedule = scheduleRepository
                .findByVesselIdAndScheduleDate(
                        vessel.getId(),
                        request.getScheduleDate())
                .orElse(null);

        if (schedule != null) {
            schedule.update(request.getType(), request.getMemo());
        } else {
            schedule = VesselSchedule.create(
                    vessel,
                    request.getScheduleDate(),
                    request.getType(),
                    request.getMemo()
            );
            scheduleRepository.save(schedule);
        }

        Integer reserved = reservationRepository
                .sumPassengersByVesselAndDate(
                        vessel.getId(),
                        request.getScheduleDate());

        return VesselScheduleResponseDto.ScheduleInfo.builder()
                .id(schedule.getId())
                .scheduleDate(schedule.getScheduleDate())
                .type(schedule.getType())
                .memo(schedule.getMemo())
                .reservedCount(reserved)
                .maxPassengers(vessel.getMaxPassengers())
                .build();
    }

    // 다중 날짜 일정 설정 (휴항 기간)
    @Transactional
    public void setMultiple(Long captainId,
                             VesselScheduleRequestDto.SetMultiple request) {
        Vessel vessel = vesselRepository
                .findById(request.getVesselId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "선박을 찾을 수 없습니다."));

        if (!vessel.getOwner().getId().equals(captainId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        request.getDates().forEach(date -> {
            VesselSchedule schedule = scheduleRepository
                    .findByVesselIdAndScheduleDate(
                            vessel.getId(), date)
                    .orElse(null);

            if (schedule != null) {
                schedule.update(request.getType(),
                                request.getMemo());
            } else {
                scheduleRepository.save(
                        VesselSchedule.create(
                                vessel, date,
                                request.getType(),
                                request.getMemo()));
            }
        });
    }

    // 일정 삭제 (초기화)
    @Transactional
    public void deleteSchedule(Long captainId, Long scheduleId) {
        VesselSchedule schedule = scheduleRepository
                .findById(scheduleId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "일정을 찾을 수 없습니다."));

        if (!schedule.getVessel().getOwner().getId()
                .equals(captainId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        scheduleRepository.delete(schedule);
    }
}