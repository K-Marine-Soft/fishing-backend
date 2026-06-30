package com.kmarine.fishing.review;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 후기 작성
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDto.Info>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReviewRequestDto.Create request) {
        return ResponseEntity.ok(
                ApiResponse.ok(reviewService.create(userId, request)));
    }

    // 선박 후기 목록 + 통계
    @GetMapping("/vessel/{vesselId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto.Summary>> getVesselReviews(
            @PathVariable("vesselId") Long vesselId) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                    reviewService.getVesselReviews(vesselId)));
    }

    // 후기 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        reviewService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}