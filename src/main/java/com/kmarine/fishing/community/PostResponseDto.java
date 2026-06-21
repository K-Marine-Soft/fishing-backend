package com.kmarine.fishing.community;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

public class PostResponseDto {

    @Getter
    @Builder
    public static class Summary {
        private Long id;
        private PostCategory category;
        private String title;
        private String authorName;
        private Integer viewCount;
        private Integer likeCount;
        private Integer commentCount;
        private String  recruitRegion;
        private String  recruitDate;
        private Integer recruitCount;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private PostCategory category;
        private String title;
        private String content;
        private String authorName;
        private Integer viewCount;
        private Integer likeCount;
        private String  recruitRegion;
        private String  recruitDate;
        private Integer recruitCount;
        private List<CommentInfo> comments;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    public static class CommentInfo {
        private Long id;
        private String authorName;
        private String content;
        private boolean deleted;
        private List<CommentInfo> replies;
        private LocalDateTime createdAt;
    }
}