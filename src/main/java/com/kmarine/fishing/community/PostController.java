package com.kmarine.fishing.community;

import com.kmarine.fishing.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDto.Summary>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PostRequestDto.Create request) {
        return ResponseEntity.ok(
                ApiResponse.ok(postService.create(userId, request)));
    }

    // 게시글 목록
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponseDto.Summary>>> getList(
            @RequestParam(required = false) PostCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok(postService.getList(category, page, size)));
    }

    // 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponseDto.Summary>>> search(
            @RequestParam(required = false) PostCategory category,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok(postService.search(category, keyword, page, size)));
    }

    // 게시글 상세
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDto.Detail>> getDetail(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok(postService.getDetail(id)));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDto.Summary>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id,
            @Valid @RequestBody PostRequestDto.Update request) {
        return ResponseEntity.ok(
                ApiResponse.ok(postService.update(userId, id, request)));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id) {
        postService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 좋아요
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> like(
            @PathVariable("id") Long id) {
        postService.like(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 댓글 작성
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<PostResponseDto.CommentInfo>> createComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") Long id,
            @Valid @RequestBody PostRequestDto.CreateComment request) {
        return ResponseEntity.ok(
                ApiResponse.ok(postService.createComment(userId, id, request)));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable("commentId") Long commentId) {
        postService.deleteComment(userId, commentId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}