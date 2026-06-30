package com.kmarine.fishing.chat;

import com.kmarine.fishing.reservation.Reservation;
import com.kmarine.fishing.reservation.ReservationRepository;
import com.kmarine.fishing.user.User;
import com.kmarine.fishing.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository    chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository        userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 생성 or 조회
    @Transactional
    public ChatMessageDto.RoomInfo getOrCreateRoom(
            Long reservationId) {

        ChatRoom room = chatRoomRepository
                .findByReservationId(reservationId)
                .orElseGet(() -> {
                    Reservation reservation =
                            reservationRepository
                                    .findById(reservationId)
                                    .orElseThrow(() ->
                                        new IllegalArgumentException(
                                            "예약을 찾을 수 없습니다."));
                    return chatRoomRepository.save(
                            ChatRoom.create(reservation));
                });

        return toRoomInfo(room, null);
    }

    // 메시지 전송
    @Transactional
    public ChatMessageDto.Response sendMessage(
            Long senderId,
            ChatMessageDto.Send request) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다."));

        ChatRoom room = chatRoomRepository
                .findById(request.getRoomId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "채팅방을 찾을 수 없습니다."));

        // 메시지 저장
        ChatMessage message = ChatMessage.create(
                room.getId(), sender, request.getContent());
        chatMessageRepository.save(message);

        // 채팅방 마지막 메시지 업데이트
        room.updateLastMessage(request.getContent());

        ChatMessageDto.Response response =
                toResponse(message);

        // WebSocket으로 실시간 전송
        messagingTemplate.convertAndSend(
                "/topic/chat/" + room.getId(), response);

        log.info("메시지 전송 roomId: {} sender: {}",
                 room.getId(), sender.getName());

        return response;
    }

    // 메시지 목록 조회
    @Transactional
    public List<ChatMessageDto.Response> getMessages(
            Long roomId, Long userId) {

        // 읽음 처리
        List<ChatMessage> messages = chatMessageRepository
                .findByRoomIdOrderByCreatedAtAsc(roomId);

        messages.stream()
                .filter(m -> !m.getSender().getId().equals(userId)
                          && !m.isRead())
                .forEach(ChatMessage::markAsRead);

        return messages.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 내 채팅방 목록
    @Transactional(readOnly = true)
    public List<ChatMessageDto.RoomInfo> getMyRooms(Long userId) {

        // 이용자 채팅방
        List<ChatRoom> userRooms = chatRoomRepository
                .findByUserIdOrderByLastMessageAtDesc(userId);

        // 선주 채팅방
        List<ChatRoom> captainRooms = chatRoomRepository
                .findByCaptainIdOrderByLastMessageAtDesc(userId);

        // 합치기 (중복 제거)
        userRooms.addAll(captainRooms);

        return userRooms.stream()
                .distinct()
                .map(room -> toRoomInfo(room, userId))
                .collect(Collectors.toList());
    }

    // 변환 메서드
    private ChatMessageDto.Response toResponse(ChatMessage m) {
        return ChatMessageDto.Response.builder()
                .id(m.getId())
                .roomId(m.getRoomId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .content(m.getContent())
                .isRead(m.isRead())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private ChatMessageDto.RoomInfo toRoomInfo(
            ChatRoom room, Long userId) {

        Integer unread = userId != null
                ? chatMessageRepository.countUnread(
                        room.getId(), userId)
                : 0;

        return ChatMessageDto.RoomInfo.builder()
                .id(room.getId())
                .reservationId(room.getReservation().getId())
                .vesselName(room.getReservation()
                        .getVessel().getName())
                .userName(room.getUser().getName())
                .captainName(room.getCaptain().getName())
                .lastMessage(room.getLastMessage())
                .lastMessageAt(room.getLastMessageAt())
                .unreadCount(unread)
                .build();
    }
}