package com.kmarine.fishing.fleet;

import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.user.UserRole;
import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.vessel.VesselRepository;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.vessel.VesselScheduleRepository;
import com.kmarine.fishing.vessel.VesselStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FleetService {

    private final FleetRepository  fleetRepository;
    private final VesselRepository vesselRepository;
    private final ReservationRepository    reservationRepository;
    private final VesselScheduleRepository scheduleRepository;
    private final UserRepository   userRepository;
    private final FleetAdminMappingRepository fleetAdminMappingRepository;
    private final FleetAdminApplicationRepository fleetAdminApplicationRepository;

    // 기존 선단에 공동관리자로 신청
    @Transactional
    public void applyForCoAdmin(Long applicantId, Long fleetId) {
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "사용자를 찾을 수 없습니다."));

        if (applicant.getRole() == UserRole.ROLE_FLEET_ADMIN) {
            throw new IllegalArgumentException("이미 선단 관리자입니다.");
        }
        if (fleetAdminMappingRepository
                .findByUserId(applicantId).isPresent()) {
            throw new IllegalArgumentException("이미 소속된 선단이 있습니다.");
        }

        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "선단을 찾을 수 없습니다."));
        if (fleet.getStatus() != FleetStatus.ACTIVE) {
            throw new IllegalArgumentException(
                "운영 중인 선단이 아닙니다.");
        }
        if (fleetAdminApplicationRepository
                .findByFleet_IdAndApplicant_IdAndStatus(
                    fleetId, applicantId,
                    FleetAdminApplicationStatus.PENDING)
                .isPresent()) {
            throw new IllegalArgumentException(
                "이미 신청한 선단입니다. 승인을 기다려주세요.");
        }

        fleetAdminApplicationRepository.save(
            FleetAdminApplication.create(fleet, applicant));
    }

    // 선단 등록 신청 (승인 전까지 PENDING)
    @Transactional
    public FleetResponseDto.Info create(Long requesterId,
            FleetRequestDto.Create request) {

        // 서브도메인 중복 체크
        if (fleetRepository.existsBySubdomain(
                request.getSubdomain())) {
            throw new IllegalArgumentException(
                "이미 사용 중인 서브도메인입니다.");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "사용자를 찾을 수 없습니다."));

        if (requester.getRole() == UserRole.ROLE_FLEET_ADMIN) {
            throw new IllegalArgumentException(
                "이미 선단 관리자입니다.");
        }
        if (fleetAdminMappingRepository
                .findByUserId(requesterId).isPresent()) {
            throw new IllegalArgumentException(
                "이미 소속된 선단이 있습니다.");
        }

        Fleet fleet = Fleet.create(request, requester);
        fleetRepository.save(fleet);
        return toInfo(fleet);
    }

    // 서브도메인으로 선단 조회
    @Transactional(readOnly = true)
    public FleetResponseDto.Info getBySubdomain(String subdomain) {
        Fleet fleet = fleetRepository
                .findBySubdomain(subdomain)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "존재하지 않는 선단입니다."));
        return toInfo(fleet);
    }

    // 도메인으로 선단 조회 (서브도메인 or 자체도메인)
    @Transactional(readOnly = true)
    public FleetResponseDto.Info getByDomain(String domain) {
        // 자체 도메인 먼저 체크
        return fleetRepository.findByCustomDomain(domain)
                .map(this::toInfo)
                .orElseGet(() ->
                    fleetRepository.findBySubdomain(
                            domain.split("\\.")[0])
                        .map(this::toInfo)
                        .orElseThrow(() ->
                            new IllegalArgumentException(
                                "존재하지 않는 선단입니다.")));
    }

    // 전체 선단 목록 (통합 플랫폼용)
    @Transactional(readOnly = true)
    public List<FleetResponseDto.Summary> getActiveFleets() {
        return fleetRepository
                .findByStatus(FleetStatus.ACTIVE)
                .stream()
                .map(f -> {
                    int vesselCount = vesselRepository
                            .findByFleet_Id(f.getId()).size();
                    return toSummary(f, vesselCount);
                })
                .collect(Collectors.toList());
    }

    // 선단 정보 수정
    @Transactional
    public FleetResponseDto.Info update(Long fleetId,
            FleetRequestDto.Update request) {
        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "선단을 찾을 수 없습니다."));
        fleet.update(request);
        return toInfo(fleet);
    }

    // 선단 등록 신청 목록 (관리자)
    @Transactional(readOnly = true)
    public List<FleetResponseDto.Info> getFleetsByStatus(
            FleetStatus status) {
        List<Fleet> fleets = status == null
                ? fleetRepository.findAll()
                : fleetRepository.findByStatus(status);
        return fleets.stream()
                .map(this::toInfo)
                .collect(Collectors.toList());
    }

    // 선단 승인 (전체 관리자) — 신청자를 선단관리자로 승격
    @Transactional
    public void activate(Long fleetId) {
        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "선단을 찾을 수 없습니다."));
        fleet.activate();

        User requester = fleet.getRequestedBy();
        if (requester != null &&
                fleetAdminMappingRepository
                    .findByUserId(requester.getId()).isEmpty()) {
            requester.updateRole(UserRole.ROLE_FLEET_ADMIN);
            fleetAdminMappingRepository.save(
                FleetAdminMapping.create(fleet, requester));
        }
    }

    // 선단 등록 거절 (전체 관리자)
    @Transactional
    public void reject(Long fleetId) {
        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "선단을 찾을 수 없습니다."));
        fleet.deactivate();
    }

    // 변환 메서드
    public FleetResponseDto.Info toInfo(Fleet f) {
        return FleetResponseDto.Info.builder()
                .id(f.getId())
                .fleetName(f.getFleetName())
                .subdomain(f.getSubdomain())
                .customDomain(f.getCustomDomain())
                .region(f.getRegion())
                .port(f.getPort())
                .address(f.getAddress())
                .phone(f.getPhone())
                .bankInfo(f.getBankInfo())
                .departurTime(f.getDeparturTime())
                .returnTime(f.getReturnTime())
                .description(f.getDescription())
                .logoUrl(f.getLogoUrl())
                .themeColor(f.getThemeColor())
                .menuConfig(f.getMenuConfig())
                .adultFare(f.getAdultFare())
                .fareIncludes(f.getFareIncludes())
                .fareExcludes(f.getFareExcludes())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .requestedById(f.getRequestedBy() != null
                        ? f.getRequestedBy().getId() : null)
                .requestedByName(f.getRequestedBy() != null
                        ? f.getRequestedBy().getName() : null)
                .requestedByEmail(f.getRequestedBy() != null
                        ? f.getRequestedBy().getEmail() : null)
                .imageUrls(f.getImages().stream()
                        .map(FleetImage::getImageUrl)
                        .collect(Collectors.toList()))
                .build();
    }

    private FleetResponseDto.Summary toSummary(Fleet f,
                                                int vesselCount) {
        return FleetResponseDto.Summary.builder()
                .id(f.getId())
                .fleetName(f.getFleetName())
                .subdomain(f.getSubdomain())
                .region(f.getRegion())
                .port(f.getPort())
                .phone(f.getPhone())
                .themeColor(f.getThemeColor())
                .logoUrl(f.getLogoUrl())
                .vesselCount(vesselCount)
                .status(f.getStatus())
                .build();
    }
	// 10일 예약현황
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReservationStatus(
            Long fleetId, int days) {

        // 선단 소속 선박 목록
        List<Vessel> vessels = vesselRepository
                .findByFleet_IdAndStatus(
                    fleetId, VesselStatus.APPROVED);

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date",     date.toString());
            dayData.put("dayOfWeek", date.getDayOfWeek()
                    .getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        java.util.Locale.KOREAN));

            List<Map<String, Object>> vesselStatuses =
                    new ArrayList<>();

            for (Vessel vessel : vessels) {
                Map<String, Object> vs = new HashMap<>();
                vs.put("vesselId",   vessel.getId());
                vs.put("vesselName", vessel.getName());
                vs.put("maxPassengers",
                       vessel.getMaxPassengers());

                // 예약 인원
                Integer reserved = reservationRepository
                        .sumPassengersByVesselAndDate(
                                vessel.getId(), date);
                vs.put("reservedCount", reserved);
                vs.put("remainCount",
                       vessel.getMaxPassengers() - reserved);

                // 일정 타입 (출조/정비/휴항)
                scheduleRepository
                        .findByVesselIdAndScheduleDate(
                                vessel.getId(), date)
                        .ifPresentOrElse(
                            s -> {
                                vs.put("scheduleType",
                                       s.getType().name());
                                vs.put("memo", s.getMemo());
                                vs.put("fishType",
                                       s.getMemo());
                            },
                            () -> {
                                vs.put("scheduleType",
                                       "AVAILABLE");
                                vs.put("memo", null);
                                vs.put("fishType", null);
                            }
                        );

                vesselStatuses.add(vs);
            }

            dayData.put("vessels", vesselStatuses);
            result.add(dayData);
        }

        return result;
    }

    // 선단 공지 목록 (최근 5개)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getNotices(Long fleetId) {
        // 게시판에서 NOTICE 카테고리 + fleet_id 조건
        // 현재는 전체 공지 반환 (fleet_id 추가 후 필터링)
        return new ArrayList<>();
    }
}