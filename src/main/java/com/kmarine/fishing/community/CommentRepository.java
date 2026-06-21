package com.kmarine.fishing.community;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 댓글 (부모 댓글만)
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);
}