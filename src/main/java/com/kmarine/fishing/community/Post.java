package com.kmarine.fishing.community;

import com.kmarine.fishing.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Integer viewCount  = 0;  // 조회수
    private Integer likeCount  = 0;  // 좋아요수
    private boolean deleted    = false; // 삭제 여부

    // 동출자 모집 전용
    private Integer recruitCount;       // 모집 인원
    private String  recruitRegion;      // 모집 지역
    private String  recruitDate;        // 출조 날짜

    @OneToMany(mappedBy = "post",
               cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static Post create(User author, PostRequestDto.Create request) {
        Post post = new Post();
        post.author        = author;
        post.category      = request.getCategory();
        post.title         = request.getTitle();
        post.content       = request.getContent();
        post.recruitCount  = request.getRecruitCount();
        post.recruitRegion = request.getRecruitRegion();
        post.recruitDate   = request.getRecruitDate();
        return post;
    }

    // 수정
    public void update(String title, String content) {
        this.title   = title;
        this.content = content;
    }

    // 조회수 증가
    public void increaseViewCount() { this.viewCount++; }

    // 좋아요
    public void increaseLikeCount() { this.likeCount++; }

    // 삭제 (soft delete)
    public void delete() { this.deleted = true; }
}