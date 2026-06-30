package com.kmarine.fishing.review;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponseDto {

    @Getter
    @Builder
    public static class Info {
        private Long id;
        private String authorName;
        private Integer rating;
        private String content;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class Summary {
        private Double avgRating;       // 평균 별점
        private Integer totalCount;     // 총 후기 수
        private Integer star5;          // 5점 수
        private Integer star4;
        private Integer star3;
        private Integer star2;
        private Integer star1;
        private List<Info> reviews;
    }
}