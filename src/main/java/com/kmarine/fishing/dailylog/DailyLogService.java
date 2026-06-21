package com.kmarine.fishing.dailylog;

import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.vessel.VesselRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyLogService {

    private final DailyLogRepository dailyLogRepository;
    private final VesselRepository   vesselRepository;
    private final UserRepository     userRepository;

    // 일지 생성 (출항 시)
    @Transactional
    public DailyLogResponseDto.Summary create(Long captainId,
                                               DailyLogRequestDto.Create request) {
        Vessel vessel = vesselRepository.findById(request.getVesselId())
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));

        User captain = userRepository.findById(captainId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 당일 일지 중복 체크
        dailyLogRepository.findByVesselIdAndLogDate(
                vessel.getId(), request.getLogDate())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("해당 날짜의 일지가 이미 존재합니다.");
                });

        DailyLog dailyLog = DailyLog.create(vessel, captain, request);

        // 위치 로그 일괄 추가 (오프라인 sync 시)
        if (request.getLocations() != null) {
            int[] seq = {1};
            request.getLocations().forEach(locReq -> {
                LocationLog loc = LocationLog.create(dailyLog, locReq, seq[0]++);
                // 조황 기록 추가
                if (locReq.getFishRecords() != null) {
                    locReq.getFishRecords().forEach(fishReq ->
                        loc.getFishRecords().add(FishRecord.create(loc, fishReq)));
                }
                dailyLog.getLocationLogs().add(loc);
            });
        }

        // 당일 경비 추가
        if (request.getExpenses() != null) {
            request.getExpenses().forEach(expReq ->
                dailyLog.getDailyExpenses().add(
                    DailyExpense.create(dailyLog, expReq)));
        }

        dailyLogRepository.save(dailyLog);
        return toSummary(dailyLog);
    }

    // 위치 추가 (이동마다)
    @Transactional
    public DailyLogResponseDto.Summary addLocation(Long captainId,
                                                    DailyLogRequestDto.AddLocation request) {
        DailyLog dailyLog = dailyLogRepository.findById(request.getDailyLogId())
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        // 권한 확인
        if (!dailyLog.getCaptain().getId().equals(captainId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 다음 순서 번호
        int nextSeq = dailyLog.getLocationLogs().size() + 1;
        LocationLog loc = LocationLog.create(dailyLog, request, nextSeq);

        // 조황 기록
        if (request.getFishRecords() != null) {
            request.getFishRecords().forEach(fishReq ->
                loc.getFishRecords().add(FishRecord.create(loc, fishReq)));
        }

        dailyLog.getLocationLogs().add(loc);
        return toSummary(dailyLog);
    }

    // 입항 처리
    @Transactional
    public DailyLogResponseDto.Summary arrive(Long captainId,
                                               DailyLogRequestDto.Arrive request) {
        DailyLog dailyLog = dailyLogRepository.findById(request.getDailyLogId())
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        if (!dailyLog.getCaptain().getId().equals(captainId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        dailyLog.arrive(request.getReturnTime());

        // 입항 시 경비 일괄 입력
        if (request.getExpenses() != null) {
            request.getExpenses().forEach(expReq ->
                dailyLog.getDailyExpenses().add(
                    DailyExpense.create(dailyLog, expReq)));
        }

        return toSummary(dailyLog);
    }

    // 오프라인 동기화
    @Transactional
    public void sync(Long captainId, DailyLogRequestDto.Sync request) {
        request.getDailyLogs().forEach(logReq -> {
            // localId 중복 체크
            boolean exists = dailyLogRepository
                    .findByVesselIdAndLogDate(request.getVesselId(), logReq.getLogDate())
                    .isPresent();

            if (!exists) {
                create(captainId, logReq);
                log.info("동기화 완료 date: {}", logReq.getLogDate());
            } else {
                log.info("이미 존재 date: {} - 스킵", logReq.getLogDate());
            }
        });
    }

    // 일지 상세 조회
    @Transactional(readOnly = true)
    public DailyLogResponseDto.Detail getDetail(Long dailyLogId) {
        DailyLog dl = dailyLogRepository.findById(dailyLogId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        return DailyLogResponseDto.Detail.builder()
                .id(dl.getId())
                .logDate(dl.getLogDate())
                .departureTime(dl.getDepartureTime())
                .returnTime(dl.getReturnTime())
                .totalPassengers(dl.getTotalPassengers())
                .memo(dl.getMemo())
                .syncStatus(dl.getSyncStatus())
                .locations(dl.getLocationLogs().stream()
                        .map(this::toLocationInfo)
                        .collect(Collectors.toList()))
                .expenses(dl.getDailyExpenses().stream()
                        .map(e -> DailyLogResponseDto.ExpenseInfo.builder()
                                .category(e.getCategory())
                                .amount(e.getAmount())
                                .memo(e.getMemo())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    // 선박별 일지 목록
    @Transactional(readOnly = true)
    public List<DailyLogResponseDto.Summary> getList(Long vesselId) {
        return dailyLogRepository.findByVesselIdOrderByLogDateDesc(vesselId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    // 물때 분석
    @Transactional(readOnly = true)
    public List<DailyLogResponseDto.TideAnalysis> analyzeTide(Long vesselId) {
        return dailyLogRepository.analyzeTide(vesselId).stream()
                .map(row -> DailyLogResponseDto.TideAnalysis.builder()
                        .tideCycle((Integer) row[0])
                        .tideType((String) row[1])
                        .avgFishCount(((Number) row[2]).doubleValue())
                        .totalCount(((Number) row[3]).intValue())
                        .visitCount(((Number) row[4]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    // 장소별 분석
    @Transactional(readOnly = true)
    public List<DailyLogResponseDto.LocationAnalysis> analyzeLocation(Long vesselId) {
        return dailyLogRepository.analyzeLocation(vesselId).stream()
                .map(row -> DailyLogResponseDto.LocationAnalysis.builder()
                        .locationName((String) row[0])
                        .latitude(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .longitude(row[2] != null ? ((Number) row[2]).doubleValue() : null)
                        .totalFishCount(((Number) row[3]).intValue())
                        .visitCount(((Number) row[4]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    // 변환 메서드
    private DailyLogResponseDto.Summary toSummary(DailyLog dl) {
        int totalFish = dl.getLocationLogs().stream()
                .flatMap(loc -> loc.getFishRecords().stream())
                .mapToInt(FishRecord::getCount)
                .sum();

        return DailyLogResponseDto.Summary.builder()
                .id(dl.getId())
                .logDate(dl.getLogDate())
                .departureTime(dl.getDepartureTime())
                .returnTime(dl.getReturnTime())
                .totalPassengers(dl.getTotalPassengers())
                .locationCount(dl.getLocationLogs().size())
                .totalFishCount(totalFish)
                .syncStatus(dl.getSyncStatus())
                .build();
    }

    private DailyLogResponseDto.LocationInfo toLocationInfo(LocationLog loc) {
        return DailyLogResponseDto.LocationInfo.builder()
                .id(loc.getId())
                .sequence(loc.getSequence())
                .recordedAt(loc.getRecordedAt())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .locationName(loc.getLocationName())
                .tideType(loc.getTideType())
                .tideCycle(loc.getTideCycle())
                .weatherCondition(loc.getWeatherCondition())
                .waterTemp(loc.getWaterTemp())
                .fishRecords(loc.getFishRecords().stream()
                        .map(f -> DailyLogResponseDto.FishInfo.builder()
                                .fishType(f.getFishType())
                                .count(f.getCount())
                                .avgSize(f.getAvgSize())
                                .method(f.getMethod())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}