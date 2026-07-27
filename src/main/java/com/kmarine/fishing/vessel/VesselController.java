package com.kmarine.fishing.vessel;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.kmarine.fishing.schedule.ScheduleResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vessels")
@RequiredArgsConstructor
public class VesselController {

    private final VesselService vesselService;

    // 선박 등록 (로그인 필요)
    @PostMapping
    public ResponseEntity<ApiResponse<VesselResponseDto.Summary>> register(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody VesselRequestDto.Register request) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.register(userId, request)));
    }

    // 선박 상세 조회 (공개)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VesselResponseDto.Detail>> getDetail(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.getDetail(id)));
    }

    // 선박 정보 수정 (선주 본인만)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VesselResponseDto.Summary>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id,
            @Valid @RequestBody VesselRequestDto.Update request) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.update(userId, id, request)));
    }

    // 선박 검색 (공개)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VesselResponseDto.Summary>>> search(
            VesselRequestDto.Search request) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.search(request)));
    }

    // 내 선박 목록 (선주)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<VesselResponseDto.Summary>>> getMyVessels(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.getMyVessels(userId)));
    }
    // 예약 가능 날짜 조회 (하위 호환)
    @GetMapping("/{id}/available-dates")
    public ResponseEntity<ApiResponse<Map<String, ScheduleResponseDto>>> getAvailableDates(
            @PathVariable("id") Long id,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                    vesselService.getAvailableDates(id, year, month)));
    }

    // 선박별 월간 예약 현황 (공개)
    @GetMapping("/{id}/reservation-status")
    public ResponseEntity<ApiResponse<List<VesselReservationStatusResponseDto>>>
            getMonthlyReservationStatus(
            @PathVariable("id") Long id,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                    vesselService.getMonthlyReservationStatus(id, year, month)));
    }

    // 선단 전체 선박의 월간 예약 현황 ("전체보기", 공개)
    @GetMapping("/reservation-status")
    public ResponseEntity<ApiResponse<List<VesselReservationStatusResponseDto>>>
            getMonthlyReservationStatusForFleet(
            @RequestParam("fleetId") Long fleetId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                    vesselService.getMonthlyReservationStatusForFleet(
                        fleetId, year, month)));
    }

    // 선박별 좌석 배치 현황 (공개)
    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<SeatMapResponseDto>> getSeatMap(
            @PathVariable("id") Long id,
            @RequestParam("date") LocalDate date) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.getSeatMap(id, date)));
    }
}