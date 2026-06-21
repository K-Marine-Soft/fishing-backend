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

    // 내 예약 목록
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto.Summary>>> getMyReservations(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(
                ApiResponse.ok(reservationService.getMyReservations(userId)));
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
}