package com.chitchat.backend.repository;

import com.chitchat.backend.entity.UserContact;
import com.chitchat.backend.entity.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserContactRepository extends JpaRepository<UserContact, UUID> {

    @Query("SELECT uc FROM UserContact uc WHERE uc.userId = :userId AND uc.status = 'ACCEPTED' AND uc.isActive = true")
    List<UserContact> findFriendsByUserId(@Param("userId") UUID userId);

    @Query("SELECT uc FROM UserContact uc WHERE uc.friendId = :userId AND uc.status = 'PENDING' AND uc.isActive = true")
    List<UserContact> findPendingRequestsByUserId(@Param("userId") UUID userId);

    @Query("SELECT uc FROM UserContact uc WHERE uc.userId = :userId AND uc.isActive = true")
    List<UserContact> findContactsByUserId(@Param("userId") UUID userId);

    Optional<UserContact> findByUserIdAndFriendId(UUID userId, UUID friendId);

    @Query("SELECT uc FROM UserContact uc WHERE uc.userId = :userId AND uc.friendId = :friendId AND uc.isActive = true")
    Optional<UserContact> findActiveContact(@Param("userId") UUID userId, @Param("friendId") UUID friendId);
}
