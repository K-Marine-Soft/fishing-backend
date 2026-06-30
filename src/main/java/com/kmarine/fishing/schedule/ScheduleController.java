package com.kmarine.fishing.schedule;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vessels")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 월별 출항 일정 조회 (공개 — 예약 화면에서도 사용)
    @GetMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<Map<String, ScheduleResponseDto>>>
            getSchedule(
            @PathVariable("id") Long id,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        return ResponseEntity.ok(ApiResponse.ok(
            scheduleService.getMonthlySchedule(id, year, month)));
    }

    // 선주 — 출항 가능/불가 설정
    @PutMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> updateSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id,
            @Valid @RequestBody ScheduleRequestDto.Update request) {
        return ResponseEntity.ok(ApiResponse.ok(
            scheduleService.updateSchedule(userId, id, request)));
    }
}
