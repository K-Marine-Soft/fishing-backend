package com.kmarine.fishing.chat;

import com.kmarine.fishing.reservation.Reservation;
import com.kmarine.fishing.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", unique = true)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;              // 이용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id")
    private User captain;           // 선주

    private LocalDateTime lastMessageAt;
    private String lastMessage;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static ChatRoom create(Reservation reservation) {
        ChatRoom room = new ChatRoom();
        room.reservation = reservation;
        room.user        = reservation.getUser();
        room.captain     = reservation.getVessel().getOwner();
        return room;
    }

    public void updateLastMessage(String message) {
        this.lastMessage   = message;
        this.lastMessageAt = LocalDateTime.now();
    }
}