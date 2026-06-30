package com.kmarine.fishing.reservation;

import com.kmarine.fishing.config.FcmService;
import com.kmarine.fishing.schedule.ScheduleService;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.vessel.VesselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository        userRepository;
    private final VesselRepository      vesselRepository;
    private final ScheduleService       scheduleService;

    private final FcmService fcmService;
    // 예약 생성
    @Transactional
    public ReservationResponseDto.Detail create(Long userId,
                                                ReservationRequestDto.Create request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Vessel vessel = vesselRepository.findById(request.getVesselId())
                .orElseThrow(() -> new IllegalArgumentException("선박을 찾을 수 없습니다."));

        scheduleService.validateDepartureAvailable(
                vessel.getId(), request.getReservationDate());

        // 정원 초과 체크
        Integer reserved = reservationRepository
                .sumPassengersByVesselAndDate(vessel.getId(), request.getReservationDate());
        if (reserved + request.getPassengerCount() > vessel.getMaxPassengers()) {
            throw new IllegalArgumentException(
                "정원이 초과됐습니다. 잔여 인원: " + (vessel.getMaxPassengers() - reserved));
        }

        // 예약 생성
        Reservation reservation = Reservation.create(user, vessel, request);

        // 탑승자 명단 추가 (선택)
        if (request.getMembers() != null) {
            request.getMembers().forEach(m ->
                reservation.getMembers().add(
                    ReservationMember.create(reservation,
                        m.getName(), m.getPhone(), m.getIdNumber())));
        }

        reservationRepository.save(reservation);
        return toDetail(reservation);
    }

    // 예약 상세 조회
    @Transactional(readOnly = true)
    public ReservationResponseDto.Detail getDetail(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 본인 예약만 조회 가능
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        return toDetail(reservation);
    }

    // 내 예약 목록
    @Transactional(readOnly = true)
    public List<ReservationResponseDto.Summary> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    // 예약 취소
    @Transactional
    public void cancel(Long userId, Long reservationId,
                       ReservationRequestDto.Cancel request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }

        reservation.cancel(request.getCancelReason());
        
     // cancel 메서드 내 취소 처리 후 추가
        reservation.cancel(request.getCancelReason());

        // 선주에게 취소 알림
        if (reservation.getVessel().getOwner()
                .getFcmToken() != null) {
            fcmService.sendToToken(
                reservation.getVessel().getOwner().getFcmToken(),
                "예약 취소 알림",
                reservation.getReservationDate() +
                " 예약이 취소됐습니다."
            );
        }
    }

    // 선주 — 선박별 예약 목록
    @Transactional(readOnly = true)
    public List<ReservationResponseDto.Summary> getVesselReservations(Long vesselId) {
        return reservationRepository.findByVesselIdOrderByReservationDateDesc(vesselId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    // 변환 메서드
    private ReservationResponseDto.Detail toDetail(Reservation r) {
        return ReservationResponseDto.Detail.builder()
                .id(r.getId())
                .vesselId(r.getVessel().getId())
                .vesselName(r.getVessel().getName())
                .region(r.getVessel().getRegion())
                .departurePort(r.getVessel().getDeparturePort())
                .reservationDate(r.getReservationDate())
                .passengerCount(r.getPassengerCount())
                .totalPrice(r.getTotalPrice())
                .status(r.getStatus())
                .cancelReason(r.getCancelReason())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private ReservationResponseDto.Summary toSummary(Reservation r) {
        return ReservationResponseDto.Summary.builder()
                .id(r.getId())
                .vesselName(r.getVessel().getName())
                .region(r.getVessel().getRegion())
                .reservationDate(r.getReservationDate())
                .passengerCount(r.getPassengerCount())
                .totalPrice(r.getTotalPrice())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}