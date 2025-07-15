package com.chitchat.backend.repository;

import com.chitchat.backend.entity.RoomParticipant;
import com.chitchat.backend.entity.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, UUID> {

    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomId = :roomId AND rp.isActive = true")
    List<RoomParticipant> findByRoomIdAndIsActiveTrue(@Param("roomId") UUID roomId);

    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.userId = :userId AND rp.isActive = true")
    List<RoomParticipant> findByUserIdAndIsActiveTrue(@Param("userId") UUID userId);

    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomId = :roomId AND rp.userId = :userId AND rp.isActive = true")
    Optional<RoomParticipant> findByRoomIdAndUserIdAndIsActiveTrue(@Param("roomId") UUID roomId, @Param("userId") UUID userId);

    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomId = :roomId AND rp.role = :role AND rp.isActive = true")
    List<RoomParticipant> findByRoomIdAndRoleAndIsActiveTrue(@Param("roomId") UUID roomId, @Param("role") ParticipantRole role);

    @Query("SELECT COUNT(rp) FROM RoomParticipant rp WHERE rp.roomId = :roomId AND rp.isActive = true")
    Long countParticipantsByRoomId(@Param("roomId") UUID roomId);

    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomId = :roomId AND rp.role = 'ADMIN' AND rp.isActive = true")
    List<RoomParticipant> findAdminsByRoomId(@Param("roomId") UUID roomId);
}
