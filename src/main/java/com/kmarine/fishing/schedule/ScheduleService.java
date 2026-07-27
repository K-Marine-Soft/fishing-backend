package com.kmarine.fishing.schedule;

import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.vessel.ScheduleType;
import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.vessel.VesselRepository;
import com.kmarine.fishing.vessel.VesselSchedule;
import com.kmarine.fishing.vessel.VesselScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final VesselScheduleRepository scheduleRepository;
    private final ReservationRepository    reservationRepository;
    private final VesselRepository         vesselRepository;

    // 월별 출항 일정 조회 (예약 인원 + 출항 가능 여부)
    @Transactional(readOnly = true)
    public Map<String, ScheduleResponseDto> getMonthlySchedule(
            Long vesselId, int year, int month) {

        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() ->
                    new IllegalArgumentException("선박을 찾을 수 없습니다."));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        Map<LocalDate, VesselSchedule> scheduleMap =
            scheduleRepository
                .findByVesselAndMonth(vessel.getId(), start, end)
                .stream()
                .collect(Collectors.toMap(
                    VesselSchedule::getScheduleDate,
                    Function.identity()));

        Map<String, ScheduleResponseDto> result = new HashMap<>();

        start.datesUntil(end.plusDays(1)).forEach(date -> {
            Integer reserved = reservationRepository
                    .sumPassengersByVesselAndDate(
                        vessel.getId(), date);

            VesselSchedule schedule = scheduleMap.get(date);
            boolean available = schedule == null
                    || schedule.getType() == ScheduleType.AVAILABLE;

            result.put(date.toString(),
                ScheduleResponseDto.builder()
                    .reserved(reserved)
                    .available(available)
                    .build());
        });

        return result;
    }

    // 예약 생성 시 출항 가능 여부 확인 (선주가 휴항/만석으로 설정한 날짜 차단)
    @Transactional(readOnly = true)
    public void validateDepartureAvailable(
            Long vesselId, LocalDate date) {

        scheduleRepository
            .findByVesselIdAndScheduleDate(vesselId, date)
            .filter(s -> s.getType() != ScheduleType.AVAILABLE)
            .ifPresent(s -> {
                throw new IllegalArgumentException(
                    "선주가 출항 불가로 설정한 날짜입니다.");
            });
    }
}
