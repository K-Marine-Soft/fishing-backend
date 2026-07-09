package com.kmarine.fishing.fleet;

import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
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
        return fleetService.toInfo(fleet);
    }

    // 내 선단 선박 목록
    @Transactional(readOnly = true)
    public List<VesselResponseDto.Summary> getMyVessels(
            Long userId) {
        Fleet fleet = getFleetByAdmin(userId);
        return vesselRepository.findByFleetId(fleet.getId())
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