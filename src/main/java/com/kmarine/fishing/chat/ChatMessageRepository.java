package com.kmarine.fishing.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    // 채팅방 메시지 목록
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // 안 읽은 메시지 수
    @Query("""
        SELECT COUNT(m) FROM ChatMessage m
        WHERE m.roomId = :roomId
        AND m.isRead = false
        AND m.sender.id != :userId
        """)
    Integer countUnread(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId);
}