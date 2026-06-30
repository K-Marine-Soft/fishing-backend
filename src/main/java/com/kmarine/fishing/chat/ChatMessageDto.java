package com.kmarine.fishing.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

public class ChatMessageDto {

    // 웹소켓 메시지 송수신
    @Getter
    @Setter
    public static class Send {
        private Long   roomId;
        private String content;
    }

    // 메시지 응답
    @Getter
    @Builder
    public static class Response {
        private Long          id;
        private Long          roomId;
        private Long          senderId;
        private String        senderName;
        private String        content;
        private boolean       isRead;
        private LocalDateTime createdAt;
    }

    // 채팅방 정보
    @Getter
    @Builder
    public static class RoomInfo {
        private Long          id;
        private Long          reservationId;
        private String        vesselName;
        private String        userName;
        private String        captainName;
        private String        lastMessage;
        private LocalDateTime lastMessageAt;
        private Integer       unreadCount;
    }
}