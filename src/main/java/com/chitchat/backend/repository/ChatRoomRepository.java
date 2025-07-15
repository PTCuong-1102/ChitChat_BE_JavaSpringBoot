package com.chitchat.backend.repository;

import com.chitchat.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @Query("SELECT cr FROM ChatRoom cr INNER JOIN cr.participants p WHERE p.userId = :userId AND p.isActive = true")
    List<ChatRoom> findRoomsByUserId(@Param("userId") UUID userId);

    @Query("SELECT cr FROM ChatRoom cr " +
            "INNER JOIN cr.participants p1 ON p1.userId = :userId1 " +
            "INNER JOIN cr.participants p2 ON p2.userId = :userId2 " +
            "WHERE cr.isGroup = false AND p1.isActive = true AND p2.isActive = true")
    Optional<ChatRoom> findDirectMessageRoom(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);

    List<ChatRoom> findByCreatorIdAndIsActiveTrue(UUID creatorId);

    List<ChatRoom> findByIsGroupTrue();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.name LIKE %:keyword% AND cr.isActive = true")
    List<ChatRoom> searchRoomsByName(@Param("keyword") String keyword);
}
