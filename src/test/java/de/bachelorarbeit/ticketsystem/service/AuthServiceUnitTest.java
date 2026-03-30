package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import de.bachelorarbeit.ticketsystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private UserAccount testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserAccount("testuser", "test@example.com", "hashedPassword", Role.ENDUSER);
    }

    @Test
    void testRegisterSuccess() {
        // Given
        String username = "newuser";
        String email = "new@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String jwtToken = "jwt.token.here";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByMail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(jwtService.generateToken(any(UserAccount.class), eq(email), eq(Role.ENDUSER))).thenReturn(jwtToken);

        // When
        String result = authService.register(username, email, password);

        // Then
        assertEquals(jwtToken, result);
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByMail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(UserAccount.class));
        verify(jwtService).generateToken(any(UserAccount.class), eq(email), eq(Role.ENDUSER));
    }

    @Test
    void testRegisterUsernameAlreadyExists() {
        // Given
        String username = "existinguser";
        String email = "new@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register(username, email, password));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).existsByMail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        // Given
        String username = "newuser";
        String email = "existing@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByMail(email)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register(username, email, password));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByMail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginWithUsernameSuccess() {
        // Given
        String username = "testuser";
        String password = "password123";
        String jwtToken = "jwt.token.here";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtService.generateToken(testUser, testUser.getMail(), testUser.getRole())).thenReturn(jwtToken);

        // When
        String result = authService.login(username, password);

        // Then
        assertEquals(jwtToken, result);
        verify(userRepository).findByUsername(username);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(testUser, testUser.getMail(), testUser.getRole());
    }

    @Test
    void testLoginWithEmailSuccess() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String jwtToken = "jwt.token.here";

        when(userRepository.findByMail(email)).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtService.generateToken(testUser, testUser.getMail(), testUser.getRole())).thenReturn(jwtToken);

        // When
        String result = authService.login(email, password);

        // Then
        assertEquals(jwtToken, result);
        verify(userRepository).findByMail(email);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(testUser, testUser.getMail(), testUser.getRole());
    }

    @Test
    void testLoginUserNotFoundByUsername() {
        // Given
        String username = "nonexistent";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.login(username, password));

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testLoginUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password123";

        when(userRepository.findByMail(email)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.login(email, password));

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository).findByMail(email);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testLoginAuthenticationFailure() {
        // Given
        String username = "testuser";
        String password = "wrongpassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new AuthenticationException("Bad credentials") {});

        // When & Then
        assertThrows(AuthenticationException.class, () -> authService.login(username, password));
        verify(userRepository).findByUsername(username);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void testChangePasswordSuccess() {
        // Given
        String username = "testuser";
        String currentPassword = "oldpassword";
        String newPassword = "newpassword";
        String confirmPassword = "newpassword";
        String encodedNewPassword = "encodedNewPassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, testUser.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // When
        assertDoesNotThrow(() -> authService.changePassword(username, currentPassword, newPassword, confirmPassword));

        // Then
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(currentPassword, testUser.getPasswordHash());
        verify(passwordEncoder).matches(newPassword, testUser.getPasswordHash());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
        assertEquals(encodedNewPassword, testUser.getPasswordHash());
    }

    @Test
    void testChangePasswordUserNotFound() {
        // Given
        String username = "nonexistent";
        String currentPassword = "oldpassword";
        String newPassword = "newpassword";
        String confirmPassword = "newpassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.changePassword(username, currentPassword, newPassword, confirmPassword));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testChangePasswordInvalidCurrentPassword() {
        // Given
        String username = "testuser";
        String currentPassword = "wrongpassword";
        String newPassword = "newpassword";
        String confirmPassword = "newpassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.changePassword(username, currentPassword, newPassword, confirmPassword));

        assertEquals("Invalid current password", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(currentPassword, testUser.getPasswordHash());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangePasswordMismatch() {
        // Given
        String username = "testuser";
        String currentPassword = "oldpassword";
        String newPassword = "newpassword";
        String confirmPassword = "differentpassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.changePassword(username, currentPassword, newPassword, confirmPassword));

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(currentPassword, testUser.getPasswordHash());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangePasswordSameAsCurrentPassword() {
        // Given
        String username = "testuser";
        String currentPassword = "oldpassword";
        String newPassword = "oldpassword";
        String confirmPassword = "oldpassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, testUser.getPasswordHash())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.changePassword(username, currentPassword, newPassword, confirmPassword));

        assertEquals("New password must be different from current password", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, times(2)).matches(anyString(), eq(testUser.getPasswordHash()));
        verify(userRepository, never()).save(any());
    }
}
