package com.kmarine.fishing.review;

import com.kmarine.fishing.reservation.Reservation;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.vessel.Vessel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false,
                unique = true)
    private Reservation reservation;  // 예약 1건당 후기 1개

    @Column(nullable = false)
    private Integer rating;           // 별점 1~5

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;           // 후기 내용

    private boolean deleted = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 생성 메서드
    public static Review create(Vessel vessel, User author,
                                Reservation reservation,
                                Integer rating, String content) {
        Review r = new Review();
        r.vessel      = vessel;
        r.author      = author;
        r.reservation = reservation;
        r.rating      = rating;
        r.content     = content;
        return r;
    }

    public void delete() { this.deleted = true; }
}