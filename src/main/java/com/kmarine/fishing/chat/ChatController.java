package com.kmarine.fishing.chat;

import com.kmarine.fishing.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // WebSocket 메시지 수신
    @MessageMapping("/chat/send")
    public void sendMessage(
            @Payload ChatMessageDto.Send request,
            Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        chatService.sendMessage(senderId, request);
    }

    // 채팅방 생성/조회
    @PostMapping("/api/chat/rooms")
    public ResponseEntity<ApiResponse<ChatMessageDto.RoomInfo>>
            getOrCreateRoom(
            @RequestParam("reservationId") Long reservationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                chatService.getOrCreateRoom(reservationId)));
    }

    // 메시지 목록
    @GetMapping("/api/chat/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageDto.Response>>> getMessages(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                chatService.getMessages(roomId, userId)));
    }

    // 내 채팅방 목록
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<ApiResponse<List<ChatMessageDto.RoomInfo>>> getMyRooms(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                chatService.getMyRooms(userId)));
    }
}