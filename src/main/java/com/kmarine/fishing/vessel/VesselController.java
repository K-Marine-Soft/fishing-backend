package com.kmarine.fishing.vessel;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vessels")
@RequiredArgsConstructor
public class VesselController {

    private final VesselService vesselService;

    // 선박 등록 (로그인 필요)
    @PostMapping
    public ResponseEntity<ApiResponse<VesselResponseDto.Summary>> register(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody VesselRequestDto.Register request) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.register(userId, request)));
    }

    // 선박 상세 조회 (공개)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VesselResponseDto.Detail>> getDetail(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.getDetail(id)));
    }

    // 선박 검색 (공개)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VesselResponseDto.Summary>>> search(
            VesselRequestDto.Search request) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.search(request)));
    }

    // 내 선박 목록 (선주)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<VesselResponseDto.Summary>>> getMyVessels(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(
                ApiResponse.ok(vesselService.getMyVessels(userId)));
    }
}