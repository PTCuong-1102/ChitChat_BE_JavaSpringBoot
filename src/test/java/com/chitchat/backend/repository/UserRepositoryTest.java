package com.chitchat.backend.repository;

import com.chitchat.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByUsername_ExistingUsername_ReturnsUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void findByUsername_NonExistingUsername_ReturnsEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByEmailOrUsername_ExistingEmail_ReturnsUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmailOrUsername("test@example.com", "test@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void findByEmailOrUsername_ExistingUsername_ReturnsUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmailOrUsername("testuser", "testuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void searchUsersByKeyword_MatchingFullName_ReturnsUsers() {
        // Given
        entityManager.persistAndFlush(testUser);
        
        User anotherUser = new User();
        anotherUser.setFullName("Another Test User");
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        entityManager.persistAndFlush(anotherUser);

        // When
        List<User> foundUsers = userRepository.searchUsersByKeyword("Test");

        // Then
        assertEquals(2, foundUsers.size());
        assertTrue(foundUsers.stream().anyMatch(user -> user.getUsername().equals("testuser")));
        assertTrue(foundUsers.stream().anyMatch(user -> user.getUsername().equals("anotheruser")));
    }

    @Test
    void searchUsersByKeyword_MatchingUsername_ReturnsUsers() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        List<User> foundUsers = userRepository.searchUsersByKeyword("testuser");

        // Then
        assertEquals(1, foundUsers.size());
        assertEquals("testuser", foundUsers.get(0).getUsername());
    }

    @Test
    void searchUsersByKeyword_NoMatch_ReturnsEmptyList() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        List<User> foundUsers = userRepository.searchUsersByKeyword("nonexistent");

        // Then
        assertTrue(foundUsers.isEmpty());
    }

    @Test
    void save_NewUser_SavesSuccessfully() {
        // Given
        User newUser = new User();
        newUser.setFullName("New User");
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("new@example.com", savedUser.getEmail());

        // Verify it's persisted
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
    }

    @Test
    void deleteById_ExistingUser_DeletesSuccessfully() {
        // Given
        User persistedUser = entityManager.persistAndFlush(testUser);
        UUID userId = persistedUser.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        Optional<User> foundUser = userRepository.findById(userId);
        assertFalse(foundUser.isPresent());
    }
}
