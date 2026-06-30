package com.kmarine.fishing.admin;

import com.kmarine.fishing.common.ApiResponse;
import com.kmarine.fishing.vessel.VesselStatus;
import jakarta.validation.Valid;
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

    // 선박 목록
    @GetMapping("/vessels")
    public ResponseEntity<ApiResponse<List<AdminResponseDto.VesselInfo>>> getVessels(
            @RequestParam(name = "status",required = false) VesselStatus status) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminService.getVesselList(status)));
    }

    // 선박 승인
    @PostMapping("/vessels/approve")
    public ResponseEntity<ApiResponse<Void>> approveVessel(
            @Valid @RequestBody AdminRequestDto.VesselApprove request) {
        adminService.approveVessel(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 선박 거절
    @PostMapping("/vessels/reject")
    public ResponseEntity<ApiResponse<Void>> rejectVessel(
            @Valid @RequestBody AdminRequestDto.VesselApprove request) {
        adminService.rejectVessel(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 정산 생성
    @PostMapping("/settlements")
    public ResponseEntity<ApiResponse<AdminResponseDto.SettlementInfo>> createSettlement(
            @Valid @RequestBody AdminRequestDto.CreateSettlement request) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminService.createSettlement(request)));
    }

    // 정산 완료
    @PostMapping("/settlements/complete")
    public ResponseEntity<ApiResponse<Void>> completeSettlement(
            @Valid @RequestBody AdminRequestDto.CompleteSettlement request) {
        adminService.completeSettlement(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 정산 목록
    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<List<AdminResponseDto.SettlementInfo>>> getSettlements(
            @RequestParam(name = "status",required = false) SettlementStatus status) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminService.getSettlements(status)));
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