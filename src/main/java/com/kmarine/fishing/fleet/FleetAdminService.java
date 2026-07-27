package com.kmarine.fishing.fleet;

import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.user.UserRole;
import com.kmarine.fishing.vessel.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FleetAdminService {

    private final FleetAdminMappingRepository mappingRepository;
    private final FleetRepository             fleetRepository;
    private final VesselRepository            vesselRepository;
    private final UserRepository              userRepository;
    private final FleetService                fleetService;
    private final FleetAdminApplicationRepository applicationRepository;

    // 내 선단 조회
    @Transactional(readOnly = true)
    public FleetResponseDto.Info getMyFleet(Long userId) {
        Fleet fleet = getFleetByAdmin(userId);
        return fleetService.toInfo(fleet);
    }

    // 내 선단 수정
    @Transactional
    public FleetResponseDto.Info updateMyFleet(
            Long userId, FleetRequestDto.Update request) {
        Fleet fleet = getFleetByAdmin(userId);
        fleet.update(request);

        if (request.getImageUrls() != null) {
            fleet.getImages().clear();
            List<String> urls = request.getImageUrls();
            for (int i = 0; i < urls.size(); i++) {
                fleet.getImages().add(
                        FleetImage.create(fleet, urls.get(i), i));
            }
        }

        return fleetService.toInfo(fleet);
    }

    // 내 선단 선박 목록
    @Transactional(readOnly = true)
    public List<VesselResponseDto.Summary> getMyVessels(
            Long userId) {
        Fleet fleet = getFleetByAdmin(userId);
        return vesselRepository.findByFleet_Id(fleet.getId())
                .stream()
                .map(v -> VesselResponseDto.Summary.builder()
                        .id(v.getId())
                        .name(v.getName())
                        .type(v.getType())
                        .status(v.getStatus())
                        .region(v.getRegion())
                        .departurePort(v.getDeparturePort())
                        .maxPassengers(v.getMaxPassengers())
                        .pricePerPerson(v.getPricePerPerson())
                        .build())
                .collect(Collectors.toList());
    }

    // 선박 등록
    @Transactional
    public VesselResponseDto.Summary registerVessel(
            Long userId,
            VesselRequestDto.Register request) {
        Fleet fleet = getFleetByAdmin(userId);
        // fleet null 체크
        if (fleet == null) {
            throw new IllegalArgumentException(
                "선단을 찾을 수 없습니다.");
        }
        User  owner = userRepository.findById(userId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다."));

        Vessel vessel = Vessel.create(owner, fleet, request);

        if (request.getOptions() != null) {
            request.getOptions().forEach(opt ->
                vessel.getOptions().add(
                    VesselOption.create(vessel, opt)));
        }

        vesselRepository.save(vessel);

        return VesselResponseDto.Summary.builder()
                .id(vessel.getId())
                .name(vessel.getName())
                .type(vessel.getType())
                .status(vessel.getStatus())
                .region(vessel.getRegion())
                .maxPassengers(vessel.getMaxPassengers())
                .pricePerPerson(vessel.getPricePerPerson())
                .build();
    }

    // 공동관리자 지정 (즉시 적용, 기존 관리자만 가능)
    @Transactional
    public void designateCoAdmin(Long adminUserId, String targetEmail) {
        Fleet fleet = getFleetByAdmin(adminUserId);

        User target = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                    "가입되지 않은 이메일입니다."));

        if (target.getRole() == UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException(
                "전체 관리자는 지정할 수 없습니다.");
        }
        if (mappingRepository.findByUserId(target.getId()).isPresent()) {
            throw new IllegalArgumentException(
                "이미 다른 선단에 소속된 사용자입니다.");
        }

        target.updateRole(UserRole.ROLE_FLEET_ADMIN);
        mappingRepository.save(
            FleetAdminMapping.create(fleet, target));
    }

    // 공동관리자 신청 목록 (내 선단으로 들어온 신청)
    @Transactional(readOnly = true)
    public List<FleetAdminApplicationResponseDto> getCoAdminApplications(
            Long adminUserId) {
        Fleet fleet = getFleetByAdmin(adminUserId);
        return applicationRepository
                .findByFleet_IdAndStatus(
                    fleet.getId(), FleetAdminApplicationStatus.PENDING)
                .stream()
                .map(a -> FleetAdminApplicationResponseDto.builder()
                        .id(a.getId())
                        .fleetId(fleet.getId())
                        .fleetName(fleet.getFleetName())
                        .applicantId(a.getApplicant().getId())
                        .applicantName(a.getApplicant().getName())
                        .applicantEmail(a.getApplicant().getEmail())
                        .status(a.getStatus())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 공동관리자 신청 승인
    @Transactional
    public void approveCoAdminApplication(Long adminUserId, Long applicationId) {
        Fleet fleet = getFleetByAdmin(adminUserId);
        FleetAdminApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "신청 내역을 찾을 수 없습니다."));

        if (!application.getFleet().getId().equals(fleet.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        application.approve();

        User applicant = application.getApplicant();
        if (mappingRepository.findByUserId(applicant.getId()).isEmpty()) {
            applicant.updateRole(UserRole.ROLE_FLEET_ADMIN);
            mappingRepository.save(
                FleetAdminMapping.create(fleet, applicant));
        }
    }

    // 공동관리자 신청 거절
    @Transactional
    public void rejectCoAdminApplication(Long adminUserId, Long applicationId) {
        Fleet fleet = getFleetByAdmin(adminUserId);
        FleetAdminApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "신청 내역을 찾을 수 없습니다."));

        if (!application.getFleet().getId().equals(fleet.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        application.reject();
    }

    // 관리자 → 선단 조회
    private Fleet getFleetByAdmin(Long userId) {
        return mappingRepository
                .findByUserId(userId)
                .map(FleetAdminMapping::getFleet)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "관리자로 등록된 선단이 없습니다."));
    }
}