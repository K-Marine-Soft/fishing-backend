package com.kmarine.fishing.review;

import jakarta.validation.constraints.*;
import lombok.Getter;

public class ReviewRequestDto {

    @Getter
    public static class Create {
        @NotNull(message = "예약 ID를 입력해주세요")
        private Long reservationId;

        @NotNull(message = "별점을 선택해주세요")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다")
        private Integer rating;

        @NotBlank(message = "후기를 입력해주세요")
        @Size(min = 10, message = "후기는 10자 이상 입력해주세요")
        private String content;
    }
}