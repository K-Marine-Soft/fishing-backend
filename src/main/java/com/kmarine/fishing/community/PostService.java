package com.kmarine.fishing.community;

import com.kmarine.fishing.common.XssUtil;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository    postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository    userRepository;

    // 게시글 작성
    @Transactional
    public PostResponseDto.Summary create(Long userId,
                                          PostRequestDto.Create request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // XSS 방어 — 제목/내용 정제
        String safeTitle   = XssUtil.sanitize(request.getTitle());
        String safeContent = XssUtil.sanitize(request.getContent());
        
        Post post = Post.create(author, request);
        // create 메서드 내부에서 safeTitle, safeContent 사용하도록
        // Post.create 수정 필요
        
        postRepository.save(post);
        return toSummary(post);
    }

    // 게시글 목록
    @Transactional(readOnly = true)
    public Page<PostResponseDto.Summary> getList(PostCategory category,
                                                  int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> posts = category == null
                ? postRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable)
                : postRepository.findByCategoryAndDeletedFalseOrderByCreatedAtDesc(
                        category, pageable);

        return posts.map(this::toSummary);
    }

    // 게시글 검색
    @Transactional(readOnly = true)
    public Page<PostResponseDto.Summary> search(PostCategory category,
                                                 String keyword,
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.search(category, keyword, pageable)
                .map(this::toSummary);
    }

    // 게시글 상세
    @Transactional
    public PostResponseDto.Detail getDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (post.isDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        post.increaseViewCount();

        // 댓글 조회
        List<Comment> comments = commentRepository
                .findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);

        return PostResponseDto.Detail.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getAuthor().getName())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .recruitRegion(post.getRecruitRegion())
                .recruitDate(post.getRecruitDate())
                .recruitCount(post.getRecruitCount())
                .comments(comments.stream()
                        .map(this::toCommentInfo)
                        .collect(Collectors.toList()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto.Summary update(Long userId, Long postId,
                                          PostRequestDto.Update request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        post.update(request.getTitle(), request.getContent());
        return toSummary(post);
    }

    // 게시글 삭제
    @Transactional
    public void delete(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        post.delete();
    }

    // 좋아요
    @Transactional
    public void like(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.increaseLikeCount();
    }

    // 댓글 작성
    @Transactional
    public PostResponseDto.CommentInfo createComment(Long userId, Long postId,
                                                      PostRequestDto.CreateComment request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 대댓글 처리
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        }

        Comment comment = Comment.create(post, author, request.getContent(), parent);
        commentRepository.save(comment);
        return toCommentInfo(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        comment.delete();
    }

    // 변환 메서드
    private PostResponseDto.Summary toSummary(Post post) {
        return PostResponseDto.Summary.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .authorName(post.getAuthor().getName())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getComments().size())
                .recruitRegion(post.getRecruitRegion())
                .recruitDate(post.getRecruitDate())
                .recruitCount(post.getRecruitCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private PostResponseDto.CommentInfo toCommentInfo(Comment comment) {
        return PostResponseDto.CommentInfo.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getName())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .deleted(comment.isDeleted())
                .replies(comment.getReplies().stream()
                        .map(this::toCommentInfo)
                        .collect(Collectors.toList()))
                .createdAt(comment.getCreatedAt())
                .build();
    }
}