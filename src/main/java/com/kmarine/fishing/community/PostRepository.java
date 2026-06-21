package com.kmarine.fishing.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 카테고리별 목록 (페이징)
    Page<Post> findByCategoryAndDeletedFalseOrderByCreatedAtDesc(
            PostCategory category, Pageable pageable);

    // 전체 목록 (페이징)
    Page<Post> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // 검색
    @Query("""
        SELECT p FROM Post p
        WHERE p.deleted = false
        AND (:category IS NULL OR p.category = :category)
        AND (p.title LIKE %:keyword%
             OR p.content LIKE %:keyword%)
        ORDER BY p.createdAt DESC
        """)
    Page<Post> search(
            @Param("category") PostCategory category,
            @Param("keyword")  String keyword,
            Pageable pageable);
}