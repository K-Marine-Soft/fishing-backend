package com.kmarine.fishing.fleet;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fleets")
@RequiredArgsConstructor
public class FleetController {

    private final FleetService fleetService;

    // 선단 등록
    @PostMapping
    public ResponseEntity<ApiResponse<FleetResponseDto.Info>>
            create(@Valid @RequestBody
                   FleetRequestDto.Create request) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetService.create(request)));
    }

    // 서브도메인으로 선단 조회
    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<ApiResponse<FleetResponseDto.Info>>
            getBySubdomain(
            @PathVariable("subdomain") String subdomain) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetService.getBySubdomain(subdomain)));
    }

    // 도메인으로 선단 조회 (프론트 라우팅용)
    @GetMapping("/domain")
    public ResponseEntity<ApiResponse<FleetResponseDto.Info>>
            getByDomain(
            @RequestParam("host") String host) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetService.getByDomain(host)));
    }

    // 전체 선단 목록 (통합 플랫폼)
    @GetMapping
    public ResponseEntity<ApiResponse
            <List<FleetResponseDto.Summary>>> getActiveFleets() {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetService.getActiveFleets()));
    }

    // 선단 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FleetResponseDto.Info>>
            update(@PathVariable("id") Long id,
                   @RequestBody FleetRequestDto.Update request) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetService.update(id, request)));
    }

    // 선단 활성화 (슈퍼 관리자)
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable("id") Long id) {
        fleetService.activate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    
 // 선단 10일 예약현황 조회
    @GetMapping("/{id}/reservation-status")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
            getReservationStatus(
            @PathVariable("id") Long fleetId,
            @RequestParam(name = "days",
                         defaultValue = "10") int days) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetService.getReservationStatus(fleetId, days)));
    }

    // 선단 공지 목록
    @GetMapping("/{id}/notices")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
            getNotices(@PathVariable("id") Long fleetId) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetService.getNotices(fleetId)));
    }
}