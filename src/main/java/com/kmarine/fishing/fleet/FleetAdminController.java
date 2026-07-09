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
}