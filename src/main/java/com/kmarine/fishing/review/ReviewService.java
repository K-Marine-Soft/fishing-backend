package com.kmarine.fishing.review;

import com.kmarine.fishing.reservation.Reservation;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.reservation.ReservationStatus;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import com.kmarine.fishing.vessel.Vessel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository      reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository        userRepository;

    // 후기 작성
    @Transactional
    public ReviewResponseDto.Info create(Long userId,
                                          ReviewRequestDto.Create request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다."));

        Reservation reservation = reservationRepository
                .findById(request.getReservationId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "예약을 찾을 수 없습니다."));

        // 본인 예약 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 완료된 예약만 후기 작성 가능
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new IllegalArgumentException(
                "출항 완료 후 후기를 작성할 수 있습니다.");
        }

        // 중복 후기 방지
        reviewRepository.findByReservationId(reservation.getId())
                .ifPresent(r -> {
                    throw new IllegalArgumentException(
                        "이미 후기를 작성했습니다.");
                });

        Review review = Review.create(
                reservation.getVessel(),
                user,
                reservation,
                request.getRating(),
                request.getContent()
        );
        reviewRepository.save(review);
        return toInfo(review);
    }

    // 선박 후기 목록 + 통계
    @Transactional(readOnly = true)
    public ReviewResponseDto.Summary getVesselReviews(Long vesselId) {

        List<Review> reviews = reviewRepository
                .findByVesselIdAndDeletedFalse(vesselId);

        Double avgRating = reviewRepository
                .avgRatingByVesselId(vesselId);

        // 별점별 개수
        List<Object[]> counts = reviewRepository
                .countByRating(vesselId);

        Map<Integer, Integer> countMap = new HashMap<>();
        counts.forEach(row -> countMap.put(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).intValue()
        ));

        return ReviewResponseDto.Summary.builder()
                .avgRating(avgRating != null
                    ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalCount(reviews.size())
                .star5(countMap.getOrDefault(5, 0))
                .star4(countMap.getOrDefault(4, 0))
                .star3(countMap.getOrDefault(3, 0))
                .star2(countMap.getOrDefault(2, 0))
                .star1(countMap.getOrDefault(1, 0))
                .reviews(reviews.stream()
                        .map(this::toInfo)
                        .collect(Collectors.toList()))
                .build();
    }

    // 후기 삭제
    @Transactional
    public void delete(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "후기를 찾을 수 없습니다."));

        if (!review.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        review.delete();
    }

    private ReviewResponseDto.Info toInfo(Review r) {
        return ReviewResponseDto.Info.builder()
                .id(r.getId())
                .authorName(r.getAuthor().getName())
                .rating(r.getRating())
                .content(r.getContent())
                .createdAt(r.getCreatedAt())
                .build();
    }
}