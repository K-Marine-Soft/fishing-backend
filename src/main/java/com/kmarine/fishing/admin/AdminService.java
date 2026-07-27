package com.kmarine.fishing.admin;

import com.kmarine.fishing.dailylog.DailyLogRepository;
import com.kmarine.fishing.expense.ExpenseRepository;
import com.kmarine.fishing.fleet.FleetResponseDto;
import com.kmarine.fishing.fleet.FleetService;
import com.kmarine.fishing.fleet.FleetStatus;
import com.kmarine.fishing.payment.PaymentRepository;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.reservation.ReservationStatus;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.vessel.VesselRepository;
import com.kmarine.fishing.vessel.VesselStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final VesselRepository      vesselRepository;
    private final UserRepository        userRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository     paymentRepository;
    private final SettlementRepository  settlementRepository;

    private final ExpenseRepository     expenseRepository;
    private final DailyLogRepository    dailyLogRepository;
    private final FleetService          fleetService;

    // 선단 등록 신청 목록
    @Transactional(readOnly = true)
    public List<FleetResponseDto.Info> getFleets(FleetStatus status) {
        return fleetService.getFleetsByStatus(status);
    }

    // 선단 승인
    @Transactional
    public void approveFleet(Long fleetId) {
        fleetService.activate(fleetId);
        log.info("선단 승인 fleetId: {}", fleetId);
    }

    // 선단 거절
    @Transactional
    public void rejectFleet(Long fleetId) {
        fleetService.reject(fleetId);
        log.info("선단 거절 fleetId: {}", fleetId);
    }

    // 대시보드
    @Transactional(readOnly = true)
    public AdminResponseDto.Dashboard getDashboard() {

        long totalUsers    = userRepository.count();
        long totalVessels  = vesselRepository.count();
        long pendingVessels = vesselRepository
                .findByStatus(VesselStatus.PENDING).size();
        long totalReservations = reservationRepository.count();
        long pendingSettlements = settlementRepository
                .findByStatus(SettlementStatus.PENDING).size();

        // 총 매출 (결제 완료 건 합계)
        Integer totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus().name().equals("PAID"))
                .mapToInt(p -> p.getAmount())
                .sum();

        return AdminResponseDto.Dashboard.builder()
                .totalUsers(totalUsers)
                .totalVessels(totalVessels)
                .pendingVessels(pendingVessels)
                .totalReservations(totalReservations)
                .totalRevenue(totalRevenue)
                .pendingSettlements(pendingSettlements)
                .build();
    }


    // 정산 생성
    @Transactional
    public AdminResponseDto.SettlementInfo createSettlement(
            AdminRequestDto.CreateSettlement request) {

        Vessel vessel = vesselRepository.findById(request.getVesselId())
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));

        // 기간 내 확정 예약 매출 합계
        Integer totalRevenue = reservationRepository
                .findByVesselIdOrderByReservationDateDesc(vessel.getId())
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .filter(r -> !r.getReservationDate().isBefore(request.getPeriodStart())
                          && !r.getReservationDate().isAfter(request.getPeriodEnd()))
                .mapToInt(r -> r.getTotalPrice())
                .sum();

        Settlement settlement = null;
        /*Settlement.create(
                vessel,
                request.getPeriodStart(),
                request.getPeriodEnd(),
                totalRevenue,
                request.getMonthlyFee(),
                request.getCommissionRate()
        );*/
        settlementRepository.save(settlement);

        return toSettlementInfo(settlement);
    }

    // 정산 완료 처리
    @Transactional
    public void completeSettlement(AdminRequestDto.CompleteSettlement request) {
        Settlement settlement = settlementRepository
                .findById(request.getSettlementId())
                .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다."));

        settlement.complete(request.getMemo());
        log.info("정산 완료 settlementId: {}", settlement.getId());
    }

    // 정산 목록
    @Transactional(readOnly = true)
    public List<AdminResponseDto.SettlementInfo> getSettlements(
            SettlementStatus status) {
        List<Settlement> settlements = status == null
                ? settlementRepository.findAll()
                : settlementRepository.findByStatus(status);

        return settlements.stream()
                .map(this::toSettlementInfo)
                .collect(Collectors.toList());
    }

    // 변환 메서드
    private AdminResponseDto.SettlementInfo toSettlementInfo(Settlement s) {
        return AdminResponseDto.SettlementInfo.builder()
                .id(s.getId())
//                .vesselName(s.getVessel().getName())
//                .ownerName(s.getVessel().getOwner().getName())
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .totalRevenue(s.getTotalRevenue())
//                .platformFee(s.getPlatformFee())
                .platformFeeAmount(s.getPlatformFeeAmount())
                .settleAmount(s.getSettleAmount())
                .status(s.getStatus())
//                .settledAt(s.getSettledAt())
                .build();
    }
    
 // 기간별 매출 통계
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlySalesStats(int year) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            final int m = month;
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end   = start.withDayOfMonth(
                                  start.lengthOfMonth());

            // 해당 월 예약 매출
            int revenue = reservationRepository.findAll()
                    .stream()
                    .filter(r -> r.getStatus() ==
                            ReservationStatus.CONFIRMED
                        || r.getStatus() ==
                            ReservationStatus.COMPLETED)
                    .filter(r -> !r.getReservationDate()
                            .isBefore(start)
                        && !r.getReservationDate()
                            .isAfter(end))
                    .mapToInt(r -> r.getTotalPrice())
                    .sum();

            // 해당 월 예약 건수
            long count = reservationRepository.findAll()
                    .stream()
                    .filter(r -> !r.getReservationDate()
                            .isBefore(start)
                        && !r.getReservationDate()
                            .isAfter(end))
                    .count();

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month",   month + "월");
            monthData.put("revenue", revenue);
            monthData.put("count",   count);
            result.add(monthData);
        }
        return result;
    }

    // 지역별 통계
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRegionStats() {
        List<Vessel> vessels = vesselRepository
                .findByStatus(VesselStatus.APPROVED);

        Map<String, Long> regionMap = vessels.stream()
                .collect(Collectors.groupingBy(
                        Vessel::getRegion,
                        Collectors.counting()));

        return regionMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("region", e.getKey());
                    m.put("count",  e.getValue());
                    return m;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("count"),
                        (Long) a.get("count")))
                .collect(Collectors.toList());
    }

    // 선박 타입별 통계
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVesselTypeStats() {
        List<Vessel> vessels = vesselRepository
                .findByStatus(VesselStatus.APPROVED);

        Map<String, Long> typeMap = vessels.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getType().name(),
                        Collectors.counting()));

        return typeMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("type",  e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // 최근 예약 목록
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentReservations(int limit) {
        return reservationRepository.findAll(
                org.springframework.data.domain.PageRequest.of(
                        0, limit,
                        org.springframework.data.domain.Sort.by(
                                "createdAt").descending()))
                .getContent()
                .stream()
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",          r.getId());
                    m.put("vesselName",  r.getVessel().getName());
                    m.put("userName",    r.getUser().getName());
                    m.put("date",        r.getReservationDate());
                    m.put("passengers",  r.getPassengerCount());
                    m.put("totalPrice",  r.getTotalPrice());
                    m.put("status",      r.getStatus().name());
                    m.put("createdAt",   r.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // 선박별 매출 순위
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVesselRevenueRanking() {
        return vesselRepository.findAll().stream()
                .map(v -> {
                    int revenue = reservationRepository
                            .findByVesselIdOrderByReservationDateDesc(
                                    v.getId())
                            .stream()
                            .filter(r -> r.getStatus() ==
                                    ReservationStatus.CONFIRMED
                                || r.getStatus() ==
                                    ReservationStatus.COMPLETED)
                            .mapToInt(r -> r.getTotalPrice())
                            .sum();

                    Map<String, Object> m = new HashMap<>();
                    m.put("vesselId",   v.getId());
                    m.put("vesselName", v.getName());
                    m.put("region",     v.getRegion());
                    m.put("revenue",    revenue);
                    return m;
                })
                .sorted((a, b) -> Integer.compare(
                        (Integer) b.get("revenue"),
                        (Integer) a.get("revenue")))
                .limit(10)
                .collect(Collectors.toList());
    }    
}