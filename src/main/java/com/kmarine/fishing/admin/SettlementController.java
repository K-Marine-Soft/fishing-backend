package com.kmarine.fishing.admin;

import com.kmarine.fishing.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    // 정산 생성
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse
            <SettlementResponseDto.Info>> generate(
            @RequestBody
            SettlementRequestDto.Generate request) {
        return ResponseEntity.ok(ApiResponse.ok(
                settlementService.generate(request)));
    }

    // 정산 완료 처리
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse
            <SettlementResponseDto.Info>> complete(
            @PathVariable("id") Long id,
            @RequestBody
            SettlementRequestDto.Complete request) {
        return ResponseEntity.ok(ApiResponse.ok(
                settlementService.complete(id, request)));
    }

    // 전체 정산 목록
    @GetMapping
    public ResponseEntity<ApiResponse
            <List<SettlementResponseDto.Info>>> getAll(
            @RequestParam(name = "status",
                         required = false)
            SettlementStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                settlementService.getAll(status)));
    }

    // 선단별 정산 내역
    @GetMapping("/fleet/{fleetId}")
    public ResponseEntity<ApiResponse
            <List<SettlementResponseDto.Info>>> getByFleet(
            @PathVariable("fleetId") Long fleetId) {
        return ResponseEntity.ok(ApiResponse.ok(
                settlementService.getByFleet(fleetId)));
    }

    // 선단별 요약
    @GetMapping("/fleet/{fleetId}/summary")
    public ResponseEntity<ApiResponse
            <SettlementResponseDto.Summary>> getSummary(
            @PathVariable("fleetId") Long fleetId) {
        return ResponseEntity.ok(ApiResponse.ok(
                settlementService.getSummary(fleetId)));
    }
}