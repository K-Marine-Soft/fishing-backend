package com.kmarine.fishing.vessel;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vessel-schedules")
@RequiredArgsConstructor
public class VesselScheduleController {

    private final VesselScheduleService scheduleService;

    // 월간 일정 조회
    @GetMapping
    public ResponseEntity<ApiResponse<VesselScheduleResponseDto.MonthlySchedule>> getMonthly(
            @RequestParam("vesselId") Long vesselId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        VesselScheduleResponseDto.MonthlySchedule result =
                scheduleService.getMonthlySchedule(
                        vesselId, year, month);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 단일 날짜 설정
    @PostMapping
    public ResponseEntity<ApiResponse<VesselScheduleResponseDto.ScheduleInfo>> setSchedule(
            @AuthenticationPrincipal Long captainId,
            @Valid @RequestBody VesselScheduleRequestDto.SetSchedule request) {

        VesselScheduleResponseDto.ScheduleInfo result =
                scheduleService.setSchedule(captainId, request);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 다중 날짜 설정
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<Void>> setMultiple(
            @AuthenticationPrincipal Long captainId,
            @Valid @RequestBody VesselScheduleRequestDto.SetMultiple request) {

        scheduleService.setMultiple(captainId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 일정 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long captainId,
            @PathVariable("id") Long id) {

        scheduleService.deleteSchedule(captainId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

/*package com.kmarine.fishing.vessel;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vessel-schedules")
@RequiredArgsConstructor
public class VesselScheduleController {

    private final VesselScheduleService scheduleService;

    // 월간 일정 조회
    @GetMapping
    public ResponseEntity<ApiResponse
            VesselScheduleResponseDto.MonthlySchedule>> getMonthly(
            @RequestParam("vesselId") Long vesselId,
            @RequestParam("year")     int year,
            @RequestParam("month")    int month) {
        return ResponseEntity.ok(ApiResponse.ok(
                scheduleService.getMonthlySchedule(
                        vesselId, year, month)));
    }

    // 단일 날짜 설정
    @PostMapping
    public ResponseEntity<ApiResponse
            VesselScheduleResponseDto.ScheduleInfo>> setSchedule(
            @AuthenticationPrincipal Long captainId,
            @Valid @RequestBody
            VesselScheduleRequestDto.SetSchedule request) {
        return ResponseEntity.ok(ApiResponse.ok(
                scheduleService.setSchedule(captainId, request)));
    }

    // 다중 날짜 설정
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<Void>> setMultiple(
            @AuthenticationPrincipal Long captainId,
            @Valid @RequestBody
            VesselScheduleRequestDto.SetMultiple request) {
        scheduleService.setMultiple(captainId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 일정 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long captainId,
            @PathVariable("id") Long id) {
        scheduleService.deleteSchedule(captainId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}*/