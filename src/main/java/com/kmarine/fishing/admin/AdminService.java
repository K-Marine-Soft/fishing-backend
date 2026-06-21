package com.kmarine.fishing.admin;

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

import java.util.List;
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

    // 선박 목록 (관리자용)
    @Transactional(readOnly = true)
    public List<AdminResponseDto.VesselInfo> getVesselList(VesselStatus status) {
        List<Vessel> vessels = status == null
                ? vesselRepository.findAll()
                : vesselRepository.findByStatus(status);

        return vessels.stream()
                .map(v -> AdminResponseDto.VesselInfo.builder()
                        .id(v.getId())
                        .name(v.getName())
                        .ownerName(v.getOwner().getName())
                        .ownerEmail(v.getOwner().getEmail())
                        .region(v.getRegion())
                        .status(v.getStatus())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 선박 승인
    @Transactional
    public void approveVessel(AdminRequestDto.VesselApprove request) {
        Vessel vessel = vesselRepository.findById(request.getVesselId())
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));
        vessel.approve();
        log.info("선박 승인 vesselId: {}", vessel.getId());
    }

    // 선박 거절
    @Transactional
    public void rejectVessel(AdminRequestDto.VesselApprove request) {
        Vessel vessel = vesselRepository.findById(request.getVesselId())
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));
        vessel.reject();
        log.info("선박 거절 vesselId: {}", vessel.getId());
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

        Settlement settlement = Settlement.create(
                vessel,
                request.getPeriodStart(),
                request.getPeriodEnd(),
                totalRevenue,
                request.getFeeRate()
        );
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
                .vesselName(s.getVessel().getName())
                .ownerName(s.getVessel().getOwner().getName())
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .totalRevenue(s.getTotalRevenue())
                .platformFee(s.getPlatformFee())
                .platformFeeAmount(s.getPlatformFeeAmount())
                .settleAmount(s.getSettleAmount())
                .status(s.getStatus())
                .settledAt(s.getSettledAt())
                .build();
    }
}