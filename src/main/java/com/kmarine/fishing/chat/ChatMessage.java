package com.kmarine.fishing.chat;

import com.kmarine.fishing.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;        // 채팅방 ID (예약 ID 기반)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;        // 발신자

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;     // 메시지 내용

    private boolean isRead = false; // 읽음 여부

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static ChatMessage create(Long roomId,
                                      User sender,
                                      String content) {
        ChatMessage m = new ChatMessage();
        m.roomId  = roomId;
        m.sender  = sender;
        m.content = content;
        return m;
    }

    public void markAsRead() { this.isRead = true; }
}