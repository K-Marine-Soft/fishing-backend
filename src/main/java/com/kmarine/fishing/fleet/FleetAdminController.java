package com.kmarine.fishing.fleet;

import com.kmarine.fishing.common.ApiResponse;
import com.kmarine.fishing.vessel.VesselRequestDto;
import com.kmarine.fishing.vessel.VesselResponseDto;
import com.kmarine.fishing.vessel.VesselService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fleet-admin")
@RequiredArgsConstructor
public class FleetAdminController {

    private final FleetAdminService fleetAdminService;
    private final VesselService     vesselService;

    // 내 선단 정보
    @GetMapping("/my-fleet")
    public ResponseEntity<ApiResponse<FleetResponseDto.Info>>
            getMyFleet(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetAdminService.getMyFleet(userId)));
    }

    // 선단 정보 수정
    @PutMapping("/my-fleet")
    public ResponseEntity<ApiResponse<FleetResponseDto.Info>>
            updateFleet(
            @AuthenticationPrincipal Long userId,
            @RequestBody FleetRequestDto.Update request) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetAdminService.updateMyFleet(
                        userId, request)));
    }

    // 내 선단 선박 목록
    @GetMapping("/vessels")
    public ResponseEntity<ApiResponse
            <List<VesselResponseDto.Summary>>> getVessels(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetAdminService.getMyVessels(userId)));
    }

    // 선박 등록
    @PostMapping("/vessels")
    public ResponseEntity<ApiResponse
            <VesselResponseDto.Summary>> registerVessel(
            @AuthenticationPrincipal Long userId,
            @RequestBody VesselRequestDto.Register request) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetAdminService.registerVessel(
                        userId, request)));
    }

    // 공동관리자 지정 (즉시 적용)
    @PostMapping("/co-admins")
    public ResponseEntity<ApiResponse<Void>> designateCoAdmin(
            @AuthenticationPrincipal Long userId,
            @RequestBody java.util.Map<String, String> body) {
        fleetAdminService.designateCoAdmin(userId, body.get("email"));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 공동관리자 신청 목록
    @GetMapping("/co-admin-applications")
    public ResponseEntity<ApiResponse
            <List<FleetAdminApplicationResponseDto>>> getCoAdminApplications(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                fleetAdminService.getCoAdminApplications(userId)));
    }

    // 공동관리자 신청 승인
    @PostMapping("/co-admin-applications/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveCoAdminApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        fleetAdminService.approveCoAdminApplication(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 공동관리자 신청 거절
    @PostMapping("/co-admin-applications/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectCoAdminApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        fleetAdminService.rejectCoAdminApplication(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}