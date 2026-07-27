package com.kmarine.fishing.reservation;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponseDto.Detail>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReservationRequestDto.Create request) {
        return ResponseEntity.ok(
                ApiResponse.ok(reservationService.create(userId, request)));
    }

    // 예약 상세
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponseDto.Detail>> getDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok(reservationService.getDetail(userId, id)));
    }

    // 내 예약 목록 (fleetId 지정 시 해당 선단 범위로 한정)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto.Summary>>> getMyReservations(
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "fleetId", required = false) Long fleetId) {
        return ResponseEntity.ok(
                ApiResponse.ok(reservationService.getMyReservations(userId, fleetId)));
    }

    // 예약 취소
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id,
            @Valid @RequestBody ReservationRequestDto.Cancel request) {
        reservationService.cancel(userId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 선박별 예약 목록 (선주용)
    @GetMapping("/vessel/{vesselId}")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto.Summary>>> getVesselReservations(
            @PathVariable("vesselId") Long vesselId) {
        return ResponseEntity.ok(
                ApiResponse.ok(reservationService.getVesselReservations(vesselId)));
    }

    // 무통장입금 등 입금 확인 후 수동 확정 (선단 관리자)
    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        reservationService.confirmPayment(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 대기 예약 수동 승격 (선단 관리자)
    @PostMapping("/{id}/promote")
    public ResponseEntity<ApiResponse<Void>> promote(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        reservationService.promote(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 출항완료 수동 처리 (선단 관리자)
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> complete(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        reservationService.completeManually(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}