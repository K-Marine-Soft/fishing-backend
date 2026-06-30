package com.kmarine.fishing.vessel;

import com.kmarine.fishing.schedule.ScheduleResponseDto;
import com.kmarine.fishing.schedule.ScheduleService;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VesselService {

    private final VesselRepository vesselRepository;
    private final UserRepository   userRepository;
    private final ScheduleService  scheduleService;

    // 선박 등록
    @Transactional
    public VesselResponseDto.Summary register(Long ownerId,
                                              VesselRequestDto.Register request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Vessel vessel = Vessel.create(owner, request);

        // 편의시설 추가
        if (request.getOptions() != null) {
            request.getOptions().forEach(opt ->
                vessel.getOptions().add(VesselOption.create(vessel, opt)));
        }

        vesselRepository.save(vessel);
        return toSummary(vessel);
    }

    // 선박 상세 조회
    @Transactional(readOnly = true)
    public VesselResponseDto.Detail getDetail(Long vesselId) {
        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));

        return VesselResponseDto.Detail.builder()
                .id(vessel.getId())
                .ownerName(vessel.getOwner().getName())
                .name(vessel.getName())
                .type(vessel.getType())
                .status(vessel.getStatus())
                .region(vessel.getRegion())
                .departurePort(vessel.getDeparturePort())
                .latitude(vessel.getLatitude())
                .longitude(vessel.getLongitude())
                .maxPassengers(vessel.getMaxPassengers())
                .pricePerPerson(vessel.getPricePerPerson())
                .description(vessel.getDescription())
                .licenseNumber(vessel.getLicenseNumber())
                .buildYear(vessel.getBuildYear())
                .vesselLength(vessel.getVesselLength())
                .enginePower(vessel.getEnginePower())
                .imageUrls(vessel.getImages().stream()
                        .map(VesselImage::getImageUrl)
                        .collect(Collectors.toList()))
                .options(vessel.getOptions().stream()
                        .map(VesselOption::getOptionName)
                        .collect(Collectors.toList()))
                .createdAt(vessel.getCreatedAt())
                .build();
    }

    // 선박 검색
    @Transactional(readOnly = true)
    public List<VesselResponseDto.Summary> search(
            VesselRequestDto.Search request) {

        List<Vessel> vessels = vesselRepository.search(
                request.getRegion(),
                request.getType(),
                request.getMinPassengers(),
                request.getMaxPrice(),
                request.getMinPrice(),
                request.getOption()
        );

        // 키워드 필터 (선박명)
        if (request.getKeyword() != null
                && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().toLowerCase();
            vessels = vessels.stream()
                    .filter(v -> v.getName().toLowerCase()
                            .contains(keyword))
                    .collect(Collectors.toList());
        }

        // 정렬
        if ("price_asc".equals(request.getSortBy())) {
            vessels.sort(Comparator.comparing(
                    Vessel::getPricePerPerson));
        } else if ("price_desc".equals(request.getSortBy())) {
            vessels.sort(Comparator.comparing(
                    Vessel::getPricePerPerson).reversed());
        } else if ("passengers".equals(request.getSortBy())) {
            vessels.sort(Comparator.comparing(
                    Vessel::getMaxPassengers).reversed());
        }

        return vessels.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
    /*
    @Transactional(readOnly = true)
    public List<VesselResponseDto.Summary> search(VesselRequestDto.Search request) {
        return vesselRepository.search(
                request.getRegion(),
                request.getType(),
                request.getMinPassengers(),
                request.getMaxPrice()
        ).stream().map(this::toSummary).collect(Collectors.toList());
    }*/

    // 내 선박 목록 (선주)
    @Transactional(readOnly = true)
    public List<VesselResponseDto.Summary> getMyVessels(Long ownerId) {
        return vesselRepository.findByOwnerId(ownerId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    // Summary 변환 공통 메서드
    private VesselResponseDto.Summary toSummary(Vessel vessel) {
        String thumbnail = vessel.getImages().stream()
                .filter(VesselImage::isThumbnail)
                .map(VesselImage::getImageUrl)
                .findFirst().orElse(null);

        return VesselResponseDto.Summary.builder()
                .id(vessel.getId())
                .name(vessel.getName())
                .type(vessel.getType())
                .status(vessel.getStatus())
                .region(vessel.getRegion())
                .departurePort(vessel.getDeparturePort())
                .maxPassengers(vessel.getMaxPassengers())
                .pricePerPerson(vessel.getPricePerPerson())
                .thumbnailUrl(thumbnail)
                .latitude(vessel.getLatitude())
                .longitude(vessel.getLongitude())
                .build();
    }
    // 월별 예약 가능 날짜 조회 (하위 호환 — schedule API 위임)
    @Transactional(readOnly = true)
    public Map<String, ScheduleResponseDto> getAvailableDates(
            Long vesselId, int year, int month) {
        return scheduleService.getMonthlySchedule(vesselId, year, month);
    }
}