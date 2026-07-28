package com.kmarine.fishing.admin;

import com.kmarine.fishing.fleet.Fleet;
import com.kmarine.fishing.fleet.FleetRepository;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.reservation.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository  settlementRepository;
    private final FleetRepository       fleetRepository;
    private final ReservationRepository reservationRepository;

    // 정산 생성 (수동)
    @Transactional
    public SettlementResponseDto.Info generate(
            SettlementRequestDto.Generate request) {

        Fleet fleet = fleetRepository
                .findById(request.getFleetId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "선단을 찾을 수 없습니다."));

        Settlement settlement = createSettlement(
                fleet, request.getPeriodStart(), request.getPeriodEnd());

        return toInfo(settlement);
    }

    // 정산 생성 공통 로직 (수동/자동 배치 공용) — 기간 내 매출 집계 후 정산 레코드 생성
    private Settlement createSettlement(
            Fleet fleet, LocalDate periodStart, LocalDate periodEnd) {

        // 중복 정산 방지
        Long existing = settlementRepository
                .countByFleetAndPeriod(
                    fleet.getId(), periodStart, periodEnd);
        if (existing > 0) {
            throw new IllegalArgumentException(
                "이미 해당 기간 정산이 존재합니다.");
        }

        // 기간 내 매출 계산
        int totalRevenue = reservationRepository
                .findAll().stream()
                .filter(r -> r.getStatus() ==
                        ReservationStatus.CONFIRMED
                    || r.getStatus() ==
                        ReservationStatus.COMPLETED)
                .filter(r -> r.getVessel().getFleet() != null
                    && r.getVessel().getFleet().getId()
                        .equals(fleet.getId()))
                .filter(r -> !r.getReservationDate()
                        .isBefore(periodStart)
                    && !r.getReservationDate()
                        .isAfter(periodEnd))
                .mapToInt(r -> r.getTotalPrice())
                .sum();

        Settlement settlement = Settlement.create(
                fleet,
                periodStart,
                periodEnd,
                totalRevenue,
                fleet.getMonthlyFee() != null
                    ? fleet.getMonthlyFee() : 0,
                fleet.getCommissionRate() != null
                    ? fleet.getCommissionRate() : 0
        );

        settlementRepository.save(settlement);
        log.info("정산 생성: 선단={} 기간={} ~ {}",
                 fleet.getFleetName(), periodStart, periodEnd);

        return settlement;
    }

    // 정산 완료 처리
    @Transactional
    public SettlementResponseDto.Info complete(
            Long id,
            SettlementRequestDto.Complete request) {

        Settlement settlement = settlementRepository
                .findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "정산을 찾을 수 없습니다."));

        settlement.complete(request.getMemo());
        log.info("정산 완료: id={}", id);
        return toInfo(settlement);
    }

    // 정산 목록 조회
    @Transactional(readOnly = true)
    public List<SettlementResponseDto.Info> getAll(
            SettlementStatus status) {
        List<Settlement> list = status != null
                ? settlementRepository.findByStatus(status)
                : settlementRepository.findAll();

        return list.stream()
                .map(this::toInfo)
                .collect(Collectors.toList());
    }

    // 선단별 정산 내역
    @Transactional(readOnly = true)
    public List<SettlementResponseDto.Info> getByFleet(
            Long fleetId) {
        return settlementRepository
                .findByFleetId(fleetId)
                .stream()
                .map(this::toInfo)
                .collect(Collectors.toList());
    }

    // 선단별 정산 요약
    @Transactional(readOnly = true)
    public SettlementResponseDto.Summary getSummary(
            Long fleetId) {

        List<Settlement> list = settlementRepository
                .findByFleetId(fleetId);

        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow();

        int totalRevenue    = list.stream()
                .mapToInt(Settlement::getTotalRevenue)
                .sum();
        int totalPlatform   = list.stream()
                .mapToInt(Settlement::getPlatformFeeAmount)
                .sum();
        int totalSettle     = list.stream()
                .mapToInt(Settlement::getSettleAmount)
                .sum();
        long pendingCount   = list.stream()
                .filter(s -> s.getStatus() ==
                        SettlementStatus.PENDING)
                .count();
        long completedCount = list.stream()
                .filter(s -> s.getStatus() ==
                        SettlementStatus.COMPLETED)
                .count();

        return SettlementResponseDto.Summary.builder()
                .fleetId(fleetId)
                .fleetName(fleet.getFleetName())
                .totalRevenue(totalRevenue)
                .totalPlatformFee(totalPlatform)
                .totalSettleAmount(totalSettle)
                .pendingCount(pendingCount)
                .completedCount(completedCount)
                .build();
    }

    // 월별 자동 정산 생성 — 실행 주기/사용여부는 batch_job_config(MONTHLY_SETTLEMENT)에서 관리 (com.kmarine.fishing.batch.DynamicBatchScheduler)
    @Transactional
    public void autoGenerateMonthlySettlements() {
        log.info("월별 자동 정산 생성 시작");

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDate start = lastMonth.withDayOfMonth(1);
        LocalDate end   = lastMonth.withDayOfMonth(
                lastMonth.lengthOfMonth());

        List<Fleet> activeFleets = fleetRepository
                .findByStatus(
                    com.kmarine.fishing.fleet.FleetStatus.ACTIVE);

        int created = 0;
        for (Fleet fleet : activeFleets) {
            try {
                Long existing = settlementRepository
                        .countByFleetAndPeriod(
                            fleet.getId(), start, end);
                if (existing > 0) {
                    log.info("자동 정산 건너뜀(이미 존재): 선단={}",
                             fleet.getFleetName());
                    continue;
                }

                createSettlement(fleet, start, end);
                created++;
            } catch (Exception e) {
                log.error("자동 정산 실패 선단={}: {}",
                          fleet.getFleetName(), e.getMessage());
            }
        }
        log.info("월별 자동 정산 생성 완료: {}건 생성", created);
    }

    // 변환 메서드
    private SettlementResponseDto.Info toInfo(Settlement s) {
        return SettlementResponseDto.Info.builder()
                .id(s.getId())
                .fleetId(s.getFleet().getId())
                .fleetName(s.getFleet().getFleetName())
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .totalRevenue(s.getTotalRevenue())
                .monthlyFee(s.getMonthlyFee())
                .commissionAmount(s.getCommissionAmount())
                .platformFeeAmount(s.getPlatformFeeAmount())
                .settleAmount(s.getSettleAmount())
                .status(s.getStatus())
                .memo(s.getMemo())
                .settledAt(s.getSettledAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}