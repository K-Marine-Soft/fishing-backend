package com.kmarine.fishing.reservation;

import com.kmarine.fishing.config.FcmService;
import com.kmarine.fishing.fleet.FleetAdminMappingRepository;
import com.kmarine.fishing.schedule.ScheduleService;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.user.UserRole;
import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.vessel.VesselRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository        userRepository;
    private final VesselRepository      vesselRepository;
    private final ScheduleService       scheduleService;
    private final FleetAdminMappingRepository fleetAdminMappingRepository;

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

        // 예약 생성
        Reservation reservation = Reservation.create(user, vessel, request);

        List<Integer> seatNumbers = request.getSeatNumbers();
        if (seatNumbers != null && !seatNumbers.isEmpty()) {
            // 좌석 지정 예약 — 좌석 단위로 중복/정원 검증 (자리를 직접 골랐으므로 대기 대상 아님)
            if (seatNumbers.size() != request.getPassengerCount()) {
                throw new IllegalArgumentException("선택한 좌석 수와 인원 수가 일치하지 않습니다.");
            }
            boolean outOfRange = seatNumbers.stream()
                    .anyMatch(n -> n < 1 || n > vessel.getMaxPassengers());
            if (outOfRange) {
                throw new IllegalArgumentException("유효하지 않은 좌석 번호입니다.");
            }
            List<Integer> occupied = reservationRepository
                    .findOccupiedSeatNumbers(vessel.getId(), request.getReservationDate());
            boolean conflict = seatNumbers.stream().anyMatch(occupied::contains);
            if (conflict) {
                throw new IllegalArgumentException("이미 선택된 좌석이 있습니다. 다시 선택해주세요.");
            }
            reservation.assignSeats(seatNumbers);
        } else {
            // 좌석 미지정 — 기존 인원수 기반 정원 검증 (초과 시 대기 등록)
            Integer reserved = reservationRepository
                    .sumPassengersByVesselAndDate(vessel.getId(), request.getReservationDate());
            boolean isFull = reserved + request.getPassengerCount() > vessel.getMaxPassengers();
            if (isFull) {
                reservation.waitlist();
            }
        }

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

    // 내 예약 목록 (fleetId가 있으면 해당 선단 범위로 한정)
    @Transactional(readOnly = true)
    public List<ReservationResponseDto.Summary> getMyReservations(
            Long userId, Long fleetId) {
        List<Reservation> reservations = fleetId == null
                ? reservationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                : reservationRepository
                    .findByUserIdAndVessel_Fleet_IdOrderByCreatedAtDesc(
                        userId, fleetId);
        return reservations
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

        // 취소로 자리가 나면 대기 1순위를 자동 승격
        promoteNextWaitlisted(
            reservation.getVessel(), reservation.getReservationDate());
    }

    // 자리가 나면 대기 순번 중 자리에 맞는 다음 건을 PENDING으로 승격
    private void promoteNextWaitlisted(Vessel vessel, LocalDate date) {
        List<Reservation> waitlist = reservationRepository
                .findByVessel_IdAndReservationDateAndStatusOrderByCreatedAtAsc(
                        vessel.getId(), date, ReservationStatus.WAITLISTED);
        if (waitlist.isEmpty()) return;

        Integer reserved = reservationRepository
                .sumPassengersByVesselAndDate(vessel.getId(), date);

        for (Reservation candidate : waitlist) {
            if (reserved + candidate.getPassengerCount() > vessel.getMaxPassengers()) {
                continue; // 이 건은 아직 자리가 부족 — 다음 대기자로 넘어가지 않고 순서 유지
            }
            candidate.promote();
            reserved += candidate.getPassengerCount();

            if (candidate.getUser().getFcmToken() != null) {
                fcmService.sendToToken(
                    candidate.getUser().getFcmToken(),
                    "대기 예약 승격 안내",
                    date + " " + vessel.getName() +
                    " 예약이 대기에서 입금 대기로 전환됐습니다. 결제를 진행해주세요."
                );
            }
        }
    }

    // 선단 관리자 권한 확인 (해당 예약 선박의 관리자이거나 전체 관리자)
    private void checkFleetAdminAccess(Long adminUserId, Reservation reservation) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean isPlatformAdmin = admin.getRole() == UserRole.ROLE_ADMIN;
        boolean isSameFleetAdmin = reservation.getVessel().getFleet() != null
                && fleetAdminMappingRepository.findByUserId(adminUserId)
                        .map(m -> m.getFleet().getId()
                                .equals(reservation.getVessel().getFleet().getId()))
                        .orElse(false);
        if (!isPlatformAdmin && !isSameFleetAdmin) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
    }

    // 무통장입금 등 입금 확인 후 수동 확정 (선단 관리자)
    @Transactional
    public void confirmPayment(Long adminUserId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        checkFleetAdminAccess(adminUserId, reservation);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("입금 대기 중인 예약이 아닙니다.");
        }

        reservation.confirm();

        if (reservation.getUser().getFcmToken() != null) {
            fcmService.sendToToken(
                reservation.getUser().getFcmToken(),
                "예약 확정 ✅",
                reservation.getVessel().getName() + " " +
                reservation.getReservationDate() + " 예약이 확정됐습니다!"
            );
        }
    }

    // 대기 예약 수동 승격 (선단 관리자)
    @Transactional
    public void promote(Long adminUserId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        checkFleetAdminAccess(adminUserId, reservation);

        if (reservation.getStatus() != ReservationStatus.WAITLISTED) {
            throw new IllegalArgumentException("대기 중인 예약이 아닙니다.");
        }

        Integer reserved = reservationRepository.sumPassengersByVesselAndDate(
                reservation.getVessel().getId(), reservation.getReservationDate());
        if (reserved + reservation.getPassengerCount()
                > reservation.getVessel().getMaxPassengers()) {
            throw new IllegalArgumentException(
                "정원 초과로 승격할 수 없습니다. 먼저 자리를 비워주세요.");
        }

        reservation.promote();

        if (reservation.getUser().getFcmToken() != null) {
            fcmService.sendToToken(
                reservation.getUser().getFcmToken(),
                "대기 예약 승격 안내",
                reservation.getReservationDate() + " " +
                reservation.getVessel().getName() +
                " 예약이 대기에서 입금 대기로 전환됐습니다. 결제를 진행해주세요."
            );
        }
    }

    // 출항완료 자동 배치 — 출조일이 지난 확정(CONFIRMED) 예약을 완료 처리. 실행 주기/사용여부는 batch_job_config(RESERVATION_AUTO_COMPLETE)에서 관리 (com.kmarine.fishing.batch.DynamicBatchScheduler)
    @Transactional
    public void autoCompletePastReservations() {
        List<Reservation> targets = reservationRepository
                .findByStatusAndReservationDateBefore(
                        ReservationStatus.CONFIRMED, LocalDate.now());
        targets.forEach(Reservation::complete);
        if (!targets.isEmpty()) {
            log.info("출항완료 자동 처리: {}건", targets.size());
        }
    }

    // 출항완료 수동 처리 (선단 관리자)
    @Transactional
    public void completeManually(Long adminUserId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        checkFleetAdminAccess(adminUserId, reservation);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("확정된 예약만 출항완료 처리할 수 있습니다.");
        }

        reservation.complete();
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
                .seatNumbers(r.getSeats().stream()
                        .map(ReservationSeat::getSeatNumber)
                        .collect(Collectors.toList()))
                .depositorName(r.getDepositorName())
                .requestMemo(r.getRequestMemo())
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