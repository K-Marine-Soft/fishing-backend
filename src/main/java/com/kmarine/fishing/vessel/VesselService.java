package com.kmarine.fishing.vessel;

import com.kmarine.fishing.fleet.Fleet;
import com.kmarine.fishing.fleet.FleetAdminMapping;
import com.kmarine.fishing.fleet.FleetAdminMappingRepository;
import com.kmarine.fishing.fleet.FleetService;
import com.kmarine.fishing.reservation.Reservation;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.reservation.ReservationStatus;
import com.kmarine.fishing.schedule.ScheduleResponseDto;
import com.kmarine.fishing.schedule.ScheduleService;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final FleetService  fleetService;
    private final FleetAdminMappingRepository fleetAdminMappingRepository;
    private final VesselScheduleRepository vesselScheduleRepository;
    private final ReservationRepository    reservationRepository;

    // 선박 등록 시 캐시 무효화
    //@CacheEvict(value = "vessel", allEntries = true)
    @CacheEvict(value = "vessel", key = "#p0")
    @Transactional
    public VesselResponseDto.Summary register(Long ownerId,
                                              VesselRequestDto.Register request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Fleet fleet = fleetAdminMappingRepository.findByUserId(ownerId)
                .map(FleetAdminMapping::getFleet)
                .orElse(null);
        Vessel vessel = Vessel.create(owner, fleet, request);

        // 편의시설 추가
        if (request.getOptions() != null) {
            request.getOptions().forEach(opt ->
                vessel.getOptions().add(VesselOption.create(vessel, opt)));
        }

        addImages(vessel, request.getImageUrls());

        vesselRepository.save(vessel);
        return toSummary(vessel);
    }

    // 이미지 목록 반영 (첫 번째를 대표 이미지로 지정)
    private void addImages(Vessel vessel, List<String> imageUrls) {
        if (imageUrls == null) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            vessel.getImages().add(VesselImage.create(
                    vessel, imageUrls.get(i), i, i == 0));
        }
    }

    // 선박 상세 조회
    // 선박 상세 조회에 캐싱
    //@Cacheable(value = "vessel", key = "#vesselId")
    @Cacheable(value = "vessel", key = "#p0")  // ← #vesselId 대신 #p0
    @Transactional(readOnly = true)
    public VesselResponseDto.Detail getDetail(Long vesselId) {
        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));

        String thumbnail = vessel.getImages().stream()
                .filter(VesselImage::isThumbnail)
                .map(VesselImage::getImageUrl)
                .findFirst().orElse(null);

        return VesselResponseDto.Detail.builder()
                .id(vessel.getId())
                .ownerName(vessel.getOwner().getName())
                .name(vessel.getName())
                .type(vessel.getType())
                .status(vessel.getStatus())
                .region(vessel.getRegion())
                .departurePort(vessel.getDeparturePort())
                .fleetSubdomain(vessel.getFleet() != null
                        ? vessel.getFleet().getSubdomain() : null)
                .latitude(vessel.getLatitude())
                .longitude(vessel.getLongitude())
                .maxPassengers(vessel.getMaxPassengers())
                .pricePerPerson(vessel.getPricePerPerson())
                .thumbnailUrl(thumbnail)
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

    // 선박 정보 수정 (선주 본인만)
    @CacheEvict(value = "vessel", key = "#p1")
    @Transactional
    public VesselResponseDto.Summary update(Long requesterId, Long vesselId,
                                            VesselRequestDto.Update request) {
        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean isOwner = vessel.getOwner().getId().equals(requesterId);
        boolean isPlatformAdmin = requester.getRole() == UserRole.ROLE_ADMIN;
        boolean isSameFleetAdmin = vessel.getFleet() != null
                && fleetAdminMappingRepository.findByUserId(requesterId)
                        .map(m -> m.getFleet().getId()
                                .equals(vessel.getFleet().getId()))
                        .orElse(false);

        if (!isOwner && !isPlatformAdmin && !isSameFleetAdmin) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        vessel.update(request);

        if (request.getOptions() != null) {
            vessel.getOptions().clear();
            request.getOptions().forEach(opt ->
                vessel.getOptions().add(VesselOption.create(vessel, opt)));
        }

        if (request.getImageUrls() != null) {
            vessel.getImages().clear();
            addImages(vessel, request.getImageUrls());
        }

        return toSummary(vessel);
    }

    // 선박 검색
    @Transactional(readOnly = true)
    public List<VesselResponseDto.Summary> search(
            VesselRequestDto.Search request) {

        List<Vessel> vessels = vesselRepository.search(
                request.getFleetId(),
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
    // 선박 수정/승인 시 캐시 무효화
//    @CacheEvict(value = "vessel", key = "#vesselId")
//    @Transactional
//    public void approve(Long vesselId) {
//        // 기존 승인 로직
//    }
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

    // 선박별 월간 예약 현황 (일자별 공지/낚시종류/입금자·입금대기·대기자 명단/취소완료/잔여석)
    @Transactional(readOnly = true)
    public List<VesselReservationStatusResponseDto> getMonthlyReservationStatus(
            Long vesselId, int year, int month) {
        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));
        return buildMonthlyStatus(List.of(vessel), year, month);
    }

    // 선단 전체 선박의 월간 예약 현황 ("전체보기")
    @Transactional(readOnly = true)
    public List<VesselReservationStatusResponseDto> getMonthlyReservationStatusForFleet(
            Long fleetId, int year, int month) {
        List<Vessel> vessels = vesselRepository.findByFleet_IdAndStatus(
                fleetId, VesselStatus.APPROVED);
        return buildMonthlyStatus(vessels, year, month);
    }

    // 특정 선박/날짜의 좌석 배치 현황 (전체 좌석 수 + 점유 좌석 번호)
    public SeatMapResponseDto getSeatMap(Long vesselId, LocalDate date) {
        Vessel vessel = vesselRepository.findById(vesselId)
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));
        List<Integer> occupied = reservationRepository
                .findOccupiedSeatNumbers(vesselId, date);
        return SeatMapResponseDto.builder()
                .maxPassengers(vessel.getMaxPassengers())
                .occupiedSeats(occupied)
                .build();
    }

    private List<VesselReservationStatusResponseDto> buildMonthlyStatus(
            List<Vessel> vessels, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        List<VesselReservationStatusResponseDto> result = new ArrayList<>();

        for (Vessel vessel : vessels) {
            Map<LocalDate, VesselSchedule> scheduleMap =
                    vesselScheduleRepository.findByVesselAndMonth(
                                    vessel.getId(), start, end)
                            .stream()
                            .collect(Collectors.toMap(
                                    VesselSchedule::getScheduleDate,
                                    s -> s, (a, b) -> a));

            Map<LocalDate, List<Reservation>> reservationsByDate =
                    reservationRepository
                            .findByVessel_IdAndReservationDateBetween(
                                    vessel.getId(), start, end)
                            .stream()
                            .collect(Collectors.groupingBy(Reservation::getReservationDate));

            start.datesUntil(end.plusDays(1)).forEach(date -> {
                VesselSchedule schedule = scheduleMap.get(date);
                List<Reservation> dayReservations = reservationsByDate
                        .getOrDefault(date, List.of());

                List<VesselReservationStatusResponseDto.ReservationEntry> confirmed =
                        entriesByStatus(dayReservations, ReservationStatus.CONFIRMED);
                List<VesselReservationStatusResponseDto.ReservationEntry> pending =
                        entriesByStatus(dayReservations, ReservationStatus.PENDING);
                List<VesselReservationStatusResponseDto.ReservationEntry> waitlisted =
                        entriesByStatus(dayReservations, ReservationStatus.WAITLISTED);
                int cancelled = sumByStatus(dayReservations, ReservationStatus.CANCELLED);

                int reservedSeats = sumByStatus(dayReservations, ReservationStatus.CONFIRMED)
                        + sumByStatus(dayReservations, ReservationStatus.PENDING);

                result.add(VesselReservationStatusResponseDto.builder()
                        .date(date)
                        .vesselId(vessel.getId())
                        .vesselName(vessel.getName())
                        .scheduleType(schedule != null
                                ? schedule.getType().name() : "AVAILABLE")
                        .memo(schedule != null ? schedule.getMemo() : null)
                        .fishType(schedule != null ? schedule.getFishType() : null)
                        .confirmedEntries(confirmed)
                        .pendingEntries(pending)
                        .waitlistedEntries(waitlisted)
                        .cancelledCount(cancelled)
                        .maxPassengers(vessel.getMaxPassengers())
                        .remainingSeats(Math.max(0,
                                vessel.getMaxPassengers() - reservedSeats))
                        .build());
            });
        }

        return result;
    }

    private List<VesselReservationStatusResponseDto.ReservationEntry> entriesByStatus(
            List<Reservation> reservations, ReservationStatus status) {
        return reservations.stream()
                .filter(r -> r.getStatus() == status)
                .map(r -> VesselReservationStatusResponseDto.ReservationEntry.builder()
                        .maskedName(maskName(r.getUser().getName()))
                        .passengerCount(r.getPassengerCount())
                        .build())
                .collect(Collectors.toList());
    }

    private int sumByStatus(List<Reservation> reservations, ReservationStatus status) {
        return reservations.stream()
                .filter(r -> r.getStatus() == status)
                .mapToInt(Reservation::getPassengerCount)
                .sum();
    }

    // 이름 마스킹 (홍길동 → 홍*동, 홍길 → 홍*)
    private String maskName(String name) {
        if (name == null || name.isBlank()) return "익명";
        int len = name.length();
        if (len == 1) return name;
        if (len == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(len - 2) + name.charAt(len - 1);
    }
}