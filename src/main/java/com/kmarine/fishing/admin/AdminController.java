package com.kmarine.fishing.admin;

import com.kmarine.fishing.common.ApiResponse;
import com.kmarine.fishing.fleet.FleetResponseDto;
import com.kmarine.fishing.fleet.FleetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 대시보드
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminResponseDto.Dashboard>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.ok(adminService.getDashboard()));
    }

    // 선단 등록 신청 목록
    @GetMapping("/fleets")
    public ResponseEntity<ApiResponse<List<FleetResponseDto.Info>>> getFleets(
            @RequestParam(name = "status", required = false) FleetStatus status) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminService.getFleets(status)));
    }

    // 선단 승인
    @PostMapping("/fleets/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveFleet(
            @PathVariable("id") Long id) {
        adminService.approveFleet(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 선단 거절
    @PostMapping("/fleets/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFleet(
            @PathVariable("id") Long id) {
        adminService.rejectFleet(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 월별 매출 통계
    @GetMapping("/stats/monthly")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
            getMonthlySales(
            @RequestParam(name = "year",
                         defaultValue = "2026") int year) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getMonthlySalesStats(year)));
    }

    // 지역별 통계
    @GetMapping("/stats/region")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
            getRegionStats() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getRegionStats()));
    }

    // 선박 타입별 통계
    @GetMapping("/stats/vessel-type")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
            getVesselTypeStats() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getVesselTypeStats()));
    }

    // 최근 예약
    @GetMapping("/stats/recent-reservations")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
            getRecentReservations(
            @RequestParam(name = "limit",
                         defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getRecentReservations(limit)));
    }

    // 선박별 매출 순위
    @GetMapping("/stats/vessel-ranking")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
            getVesselRanking() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getVesselRevenueRanking()));
    }
}