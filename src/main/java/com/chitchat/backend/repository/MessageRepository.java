package com.chitchat.backend.repository;

import com.chitchat.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.isActive = true ORDER BY m.sentAt DESC")
    Page<Message> findByRoomIdOrderBySentAtDesc(@Param("roomId") UUID roomId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND m.isActive = true ORDER BY m.sentAt DESC")
    Page<Message> searchMessagesInRoom(@Param("roomId") UUID roomId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND m.isActive = true ORDER BY m.sentAt DESC")
    List<Message> searchMessagesInRoomList(@Param("roomId") UUID roomId, @Param("keyword") String keyword);

    @Query("SELECT m FROM Message m WHERE m.senderId = :senderId AND m.isActive = true")
    List<Message> findBySenderIdAndIsActiveTrue(@Param("senderId") UUID senderId);

    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.sentAt >= :since AND m.isActive = true ORDER BY m.sentAt ASC")
    List<Message> findRecentMessages(@Param("roomId") UUID roomId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.roomId = :roomId AND m.isActive = true")
    Long countMessagesByRoomId(@Param("roomId") UUID roomId);

    @Query("SELECT m FROM Message m " +
           "JOIN RoomParticipant rp ON rp.roomId = m.roomId " +
           "WHERE rp.userId = :userId AND rp.isActive = true " +
           "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND m.isActive = true " +
           "ORDER BY m.sentAt DESC")
    Page<Message> searchMessagesAcrossUserRooms(@Param("userId") UUID userId, @Param("keyword") String keyword, Pageable pageable);
}
