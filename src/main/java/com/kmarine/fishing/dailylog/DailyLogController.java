package com.kmarine.fishing.dailylog;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/daily-logs")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogService dailyLogService;

    // 일지 생성 (출항 시)
    @PostMapping
    public ResponseEntity<ApiResponse<DailyLogResponseDto.Summary>> create(
            @AuthenticationPrincipal Long captainId,
            @Valid @RequestBody DailyLogRequestDto.Create request) {
        return ResponseEntity.ok(
                ApiResponse.ok(dailyLogService.create(captainId, request)));
    }

    // 위치 추가 (이동마다)
    @PostMapping("/location")
    public ResponseEntity<ApiResponse<DailyLogResponseDto.Summary>> addLocation(
            @AuthenticationPrincipal Long captainId,
            @RequestBody DailyLogRequestDto.AddLocation request) {
        return ResponseEntity.ok(
                ApiResponse.ok(dailyLogService.addLocation(captainId, request)));
    }

    // 입항 처리
    @PostMapping("/arrive")
    public ResponseEntity<ApiResponse<DailyLogResponseDto.Summary>> arrive(
            @AuthenticationPrincipal Long captainId,
            @RequestBody DailyLogRequestDto.Arrive request) {
        return ResponseEntity.ok(
                ApiResponse.ok(dailyLogService.arrive(captainId, request)));
    }

    // 오프라인 동기화 (입항 후)
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Void>> sync(
            @AuthenticationPrincipal Long captainId,
            @Valid @RequestBody DailyLogRequestDto.Sync request) {
        dailyLogService.sync(captainId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 일지 상세
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyLogResponseDto.Detail>> getDetail(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok(dailyLogService.getDetail(id)));
    }

    // 선박별 일지 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyLogResponseDto.Summary>>> getList(
            @RequestParam("vesselId") Long vesselId) {
        return ResponseEntity.ok(
                ApiResponse.ok(dailyLogService.getList(vesselId)));
    }

    // 물때 분석
    @GetMapping("/analyze/tide")
    public ResponseEntity<ApiResponse<List<DailyLogResponseDto.TideAnalysis>>> analyzeTide(
            @RequestParam("vesselId") Long vesselId) {
        return ResponseEntity.ok(
                ApiResponse.ok(dailyLogService.analyzeTide(vesselId)));
    }

    // 장소별 분석
    @GetMapping("/analyze/location")
    public ResponseEntity<ApiResponse<List<DailyLogResponseDto.LocationAnalysis>>> analyzeLocation(
            @RequestParam("vesselId") Long vesselId) {
        return ResponseEntity.ok(
                ApiResponse.ok(dailyLogService.analyzeLocation(vesselId)));
    }
}