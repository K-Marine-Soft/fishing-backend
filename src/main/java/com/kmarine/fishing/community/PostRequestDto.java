package com.kmarine.fishing.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class PostRequestDto {

    @Getter
    public static class Create {
        @NotNull(message = "카테고리를 선택해주세요")
        private PostCategory category;

        @NotBlank(message = "제목을 입력해주세요")
        private String title;

        @NotBlank(message = "내용을 입력해주세요")
        private String content;

        // 동출자 모집 전용
        private Integer recruitCount;
        private String  recruitRegion;
        private String  recruitDate;
    }

    @Getter
    public static class Update {
        @NotBlank(message = "제목을 입력해주세요")
        private String title;

        @NotBlank(message = "내용을 입력해주세요")
        private String content;
    }

    @Getter
    public static class CreateComment {
        @NotBlank(message = "내용을 입력해주세요")
        private String content;

        private Long parentId;  // 대댓글 시 부모 댓글 ID
    }
}