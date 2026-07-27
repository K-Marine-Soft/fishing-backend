package com.kmarine.fishing.batch;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 배치(스케줄) 작업의 등록/수정/사용여부를 관리자가 웹에서 제어하는 API
@RestController
@RequestMapping("/api/admin/batch-jobs")
@RequiredArgsConstructor
public class BatchJobController {

    private final BatchJobService batchJobService;

    // 배치 작업 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<BatchJobResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(batchJobService.getAll()));
    }

    // 신규 스케줄 등록
    @PostMapping("/{jobKey}")
    public ResponseEntity<ApiResponse<Void>> create(
            @PathVariable("jobKey") BatchJobKey jobKey,
            @Valid @RequestBody BatchJobRequestDto.Create request) {
        batchJobService.create(jobKey, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 실행 주기(cron) / 사용여부 / 설명 수정 — 저장 즉시 스케줄에 반영됨
    @PutMapping("/{jobKey}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable("jobKey") BatchJobKey jobKey,
            @Valid @RequestBody BatchJobRequestDto.Update request) {
        batchJobService.update(jobKey, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 사용/미사용만 빠르게 전환
    @PatchMapping("/{jobKey}/enabled")
    public ResponseEntity<ApiResponse<Void>> setEnabled(
            @PathVariable("jobKey") BatchJobKey jobKey,
            @RequestBody BatchJobRequestDto.EnabledUpdate request) {
        batchJobService.setEnabled(jobKey, request.isEnabled());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 등록 해제
    @DeleteMapping("/{jobKey}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("jobKey") BatchJobKey jobKey) {
        batchJobService.delete(jobKey);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 지금 즉시 1회 수동 실행 (사용여부와 무관)
    @PostMapping("/{jobKey}/run-now")
    public ResponseEntity<ApiResponse<Void>> runNow(
            @PathVariable("jobKey") BatchJobKey jobKey) {
        batchJobService.runNow(jobKey);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 배치 실행 로그 조회
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<BatchJobLogResponseDto>>> getLogs(
            @RequestParam(name = "jobKey", required = false) BatchJobKey jobKey,
            @RequestParam(name = "status", required = false) BatchJobLogStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok(batchJobService.getLogs(jobKey, status, page, size)));
    }

    // 실행 로그 개별 삭제
    @DeleteMapping("/logs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLog(
            @PathVariable("id") Long id) {
        batchJobService.deleteLog(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 실행 로그 일괄 삭제 (체크박스로 선택한 항목)
    @DeleteMapping("/logs")
    public ResponseEntity<ApiResponse<Void>> deleteLogs(
            @RequestParam("ids") List<Long> ids) {
        batchJobService.deleteLogs(ids);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
