package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.RegisterRequest;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import de.bachelorarbeit.ticketsystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterSuccess() {
        // Test successful registration
        String token = authService.register("testuser", "test@example.com", "password123");

        assertNotNull(token);
        assertTrue(userRepository.existsByMail("test@example.com"));
        assertTrue(userRepository.existsByUsername("testuser"));
    }

    @Test
    void testRegisterDuplicateEmail() {
        // First registration should succeed
        authService.register("testuser1", "test@example.com", "password123");

        // Second registration with same email should fail
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("testuser2", "test@example.com", "password456");
        });
    }

    @Test
    void testRegisterDuplicateUsername() {
        // First registration should succeed
        authService.register("testuser", "test1@example.com", "password123");

        // Second registration with same username should fail
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("testuser", "test2@example.com", "password456");
        });
    }

    @Test
    void testLoginWithUsername() {
        // First register a user
        authService.register("testuser", "test@example.com", "password123");

        // Test login with username
        String token = authService.login("testuser", "password123");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testLoginWithEmail() {
        // First register a user
        authService.register("testuser", "test@example.com", "password123");

        // Test login with email
        String token = authService.login("test@example.com", "password123");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        // First register a user
        authService.register("testuser", "test@example.com", "password123");

        // Test login with wrong password
        assertThrows(Exception.class, () -> {
            authService.login("testuser", "wrongpassword");
        });

        // Test login with non-existent username
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login("nonexistent", "password123");
        });

        // Test login with non-existent email
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login("nonexistent@example.com", "password123");
        });
    }

    @Test
    void testChangePasswordSuccess() {
        // First register a user
        authService.register("testuser", "test@example.com", "password123");

        // Test successful password change
        assertDoesNotThrow(() -> {
            authService.changePassword("testuser", "password123", "newpassword123", "newpassword123");
        });

        // Verify user can login with new password
        String token = authService.login("testuser", "newpassword123");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify user cannot login with old password
        assertThrows(Exception.class, () -> {
            authService.login("testuser", "password123");
        });
    }

    @Test
    void testChangePasswordInvalidCurrentPassword() {
        // First register a user
        authService.register("testuser", "test@example.com", "password123");

        // Test change password with wrong current password
        assertThrows(IllegalArgumentException.class, () -> {
            authService.changePassword("testuser", "wrongpassword", "newpassword123", "newpassword123");
        });
    }

    @Test
    void testChangePasswordMismatch() {
        // First register a user
        authService.register("testuser", "test@example.com", "password123");

        // Test change password with mismatched new passwords
        assertThrows(IllegalArgumentException.class, () -> {
            authService.changePassword("testuser", "password123", "newpassword123", "differentpassword");
        });
    }

    @Test
    void testChangePasswordUserNotFound() {
        // Test change password for non-existent user
        assertThrows(IllegalArgumentException.class, () -> {
            authService.changePassword("nonexistent", "password123", "newpassword123", "newpassword123");
        });
    }

    @Test
    void testChangePasswordSameAsCurrentPassword() {
        // First register a user
        authService.register("testuser", "test@example.com", "password123");

        // Test change password to same password
        assertThrows(IllegalArgumentException.class, () -> {
            authService.changePassword("testuser", "password123", "password123", "password123");
        });
    }
}
