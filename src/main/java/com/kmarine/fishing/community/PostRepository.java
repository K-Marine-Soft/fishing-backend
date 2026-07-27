package com.kmarine.fishing.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 선단 + 카테고리별 목록 (페이징)
    Page<Post> findByFleet_IdAndCategoryAndDeletedFalseOrderByCreatedAtDesc(
            Long fleetId, PostCategory category, Pageable pageable);

    // 선단 전체 목록 (페이징)
    Page<Post> findByFleet_IdAndDeletedFalseOrderByCreatedAtDesc(
            Long fleetId, Pageable pageable);

    // 검색 (선단 범위 내)
    @Query("""
        SELECT p FROM Post p
        WHERE p.deleted = false
        AND p.fleet.id = :fleetId
        AND (:category IS NULL OR p.category = :category)
        AND (p.title LIKE %:keyword%
             OR p.content LIKE %:keyword%)
        ORDER BY p.createdAt DESC
        """)
    Page<Post> search(
            @Param("fleetId")  Long fleetId,
            @Param("category") PostCategory category,
            @Param("keyword")  String keyword,
            Pageable pageable);
}
