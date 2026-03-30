package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.service.AuthService;
import de.bachelorarbeit.ticketsystem.security.JwtService;
import de.bachelorarbeit.ticketsystem.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithAnonymousUser
    void testRegisterSuccess() throws Exception {
        // Given
        String token = "jwt.token.here";
        when(authService.register("testuser", "test@example.com", "password123")).thenReturn(token);

        String requestBody = """
            {
                "username": "testuser",
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(authService).register("testuser", "test@example.com", "password123");
    }

    @Test
    @WithAnonymousUser
    void testRegisterUsernameAlreadyExists() throws Exception {
        // Given
        when(authService.register("existinguser", "test@example.com", "password123"))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        String requestBody = """
            {
                "username": "existinguser",
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(authService).register("existinguser", "test@example.com", "password123");
    }

    @Test
    @WithAnonymousUser
    void testRegisterEmailAlreadyExists() throws Exception {
        // Given
        when(authService.register("testuser", "existing@example.com", "password123"))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        String requestBody = """
            {
                "username": "testuser",
                "email": "existing@example.com",
                "password": "password123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(authService).register("testuser", "existing@example.com", "password123");
    }

    @Test
    @WithAnonymousUser
    void testRegisterInvalidRequest() throws Exception {
        // Given - missing required fields
        String requestBody = """
            {
                "username": "",
                "email": "invalid-email",
                "password": ""
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    @WithAnonymousUser
    void testLoginSuccess() throws Exception {
        // Given
        String token = "jwt.token.here";
        when(authService.login("testuser", "password123")).thenReturn(token);

        String requestBody = """
            {
                "login": "testuser",
                "password": "password123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));

        verify(authService).login("testuser", "password123");
    }

    @Test
    @WithAnonymousUser
    void testLoginWithEmail() throws Exception {
        // Given
        String token = "jwt.token.here";
        when(authService.login("test@example.com", "password123")).thenReturn(token);

        String requestBody = """
            {
                "login": "test@example.com",
                "password": "password123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));

        verify(authService).login("test@example.com", "password123");
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        // Given
        when(authService.login("testuser", "wrongpassword"))
                .thenThrow(new IllegalArgumentException("User not found with username: testuser"));

        String requestBody = """
            {
                "login": "testuser",
                "password": "wrongpassword"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(authService).login("testuser", "wrongpassword");
    }

    @Test
    void testLoginInvalidRequest() throws Exception {
        // Given - missing required fields
        String requestBody = """
            {
                "login": "",
                "password": ""
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testChangePasswordSuccess() throws Exception {
        // Given
        doNothing().when(authService).changePassword("testuser", "oldpassword", "newpassword", "newpassword");

        String requestBody = """
            {
                "currentPassword": "oldpassword",
                "newPassword": "newpassword",
                "confirmPassword": "newpassword"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Passwort erfolgreich geändert"));

        verify(authService).changePassword("testuser", "oldpassword", "newpassword", "newpassword");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testChangePasswordMismatch() throws Exception {
        // Given - passwords don't match in request
        String requestBody = """
            {
                "currentPassword": "oldpassword",
                "newPassword": "newpassword",
                "confirmPassword": "differentpassword"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validierung fehlgeschlagen"))
                .andExpect(jsonPath("$.errors.confirmPassword").value("Passwörter stimmen nicht überein"));

        verify(authService, never()).changePassword(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testChangePasswordInvalidCurrentPassword() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Invalid current password"))
                .when(authService).changePassword("testuser", "wrongpassword", "newpassword", "newpassword");

        String requestBody = """
            {
                "currentPassword": "wrongpassword",
                "newPassword": "newpassword",
                "confirmPassword": "newpassword"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ungültiges aktuelles Passwort"))
                .andExpect(jsonPath("$.errors.currentPassword").value("Ungültiges aktuelles Passwort"));

        verify(authService).changePassword("testuser", "wrongpassword", "newpassword", "newpassword");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testChangePasswordSameAsCurrentPassword() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("New password must be different from current password"))
                .when(authService).changePassword("testuser", "password123", "password123", "password123");

        String requestBody = """
            {
                "currentPassword": "password123",
                "newPassword": "password123",
                "confirmPassword": "password123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Das neue Passwort muss sich vom aktuellen Passwort unterscheiden"))
                .andExpect(jsonPath("$.errors.newPassword").value("Das neue Passwort muss sich vom aktuellen Passwort unterscheiden"));

        verify(authService).changePassword("testuser", "password123", "password123", "password123");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testChangePasswordServicePasswordMismatch() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Passwords do not match"))
                .when(authService).changePassword("testuser", "oldpassword", "newpassword", "newpassword");

        String requestBody = """
            {
                "currentPassword": "oldpassword",
                "newPassword": "newpassword",
                "confirmPassword": "newpassword"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Passwörter stimmen nicht überein"))
                .andExpect(jsonPath("$.errors.confirmPassword").value("Passwörter stimmen nicht überein"));

        verify(authService).changePassword("testuser", "oldpassword", "newpassword", "newpassword");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testChangePasswordInvalidRequest() throws Exception {
        // Given - missing required fields
        String requestBody = """
            {
                "currentPassword": "",
                "newPassword": "",
                "confirmPassword": ""
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).changePassword(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testChangePasswordWithoutAuthentication() throws Exception {
        // Given - no authentication
        String requestBody = """
            {
                "currentPassword": "oldpassword",
                "newPassword": "newpassword",
                "confirmPassword": "newpassword"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).changePassword(anyString(), anyString(), anyString(), anyString());
    }
}
