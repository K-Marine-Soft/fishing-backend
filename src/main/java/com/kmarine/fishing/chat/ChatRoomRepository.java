package com.kmarine.fishing.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository
        extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByReservationId(Long reservationId);

    // 내 채팅방 목록 (이용자)
    List<ChatRoom> findByUserIdOrderByLastMessageAtDesc(Long userId);

    // 내 채팅방 목록 (선주)
    List<ChatRoom> findByCaptainIdOrderByLastMessageAtDesc(
            Long captainId);
}