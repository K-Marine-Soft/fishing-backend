package com.kmarine.fishing.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    // 선박별 후기 목록
    List<Review> findByVesselIdAndDeletedFalse(Long vesselId);

    // 예약별 후기 (중복 방지)
    Optional<Review> findByReservationId(Long reservationId);

    // 선박 평균 별점
    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.vessel.id = :vesselId
        AND r.deleted = false
        """)
    Double avgRatingByVesselId(@Param("vesselId") Long vesselId);

    // 별점별 개수
    @Query("""
        SELECT r.rating, COUNT(r)
        FROM Review r
        WHERE r.vessel.id = :vesselId
        AND r.deleted = false
        GROUP BY r.rating
        ORDER BY r.rating DESC
        """)
    List<Object[]> countByRating(@Param("vesselId") Long vesselId);
}