package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.UserResponse;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMINUSER")
    void testGetAllUsersSuccess() throws Exception {
        // Given
        List<UserResponse> users = Arrays.asList(
            new UserResponse("user1@example.com", "user1", Role.ENDUSER),
            new UserResponse("user2@example.com", "user2", Role.SUPPORTUSER),
            new UserResponse("admin@example.com", "admin", Role.ADMINUSER)
        );
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].mail").value("user1@example.com"))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].role").value("ENDUSER"))
                .andExpect(jsonPath("$[1].mail").value("user2@example.com"))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[1].role").value("SUPPORTUSER"))
                .andExpect(jsonPath("$[2].mail").value("admin@example.com"))
                .andExpect(jsonPath("$[2].username").value("admin"))
                .andExpect(jsonPath("$[2].role").value("ADMINUSER"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMINUSER")
    void testGetAllUsersEmptyList() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "SUPPORTUSER")
    void testGetAllUsersAccessDeniedForSupportUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ENDUSER")
    void testGetAllUsersAccessDeniedForEndUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    @Test
    void testGetAllUsersUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMINUSER")
    void testUpdateUserRoleSuccess() throws Exception {
        // Given
        String email = "user@example.com";
        Role newRole = Role.SUPPORTUSER;
        UserResponse updatedUser = new UserResponse(email, "testuser", newRole);

        when(userService.updateUserRole(email, newRole)).thenReturn(updatedUser);

        String requestBody = """
            {
                "role": "SUPPORTUSER"
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value(email))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("SUPPORTUSER"));

        verify(userService).updateUserRole(email, newRole);
    }

    @Test
    @WithMockUser(roles = "ADMINUSER")
    void testUpdateUserRoleToEndUser() throws Exception {
        // Given
        String email = "support@example.com";
        Role newRole = Role.ENDUSER;
        UserResponse updatedUser = new UserResponse(email, "supportuser", newRole);

        when(userService.updateUserRole(email, newRole)).thenReturn(updatedUser);

        String requestBody = """
            {
                "role": "ENDUSER"
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value(email))
                .andExpect(jsonPath("$.username").value("supportuser"))
                .andExpect(jsonPath("$.role").value("ENDUSER"));

        verify(userService).updateUserRole(email, newRole);
    }

    @Test
    @WithMockUser(roles = "ADMINUSER")
    void testUpdateUserRoleToAdminUser() throws Exception {
        // Given
        String email = "user@example.com";
        Role newRole = Role.ADMINUSER;
        UserResponse updatedUser = new UserResponse(email, "user", newRole);

        when(userService.updateUserRole(email, newRole)).thenReturn(updatedUser);

        String requestBody = """
            {
                "role": "ADMINUSER"
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value(email))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.role").value("ADMINUSER"));

        verify(userService).updateUserRole(email, newRole);
    }

    @Test
    @WithMockUser(roles = "ADMINUSER")
    void testUpdateUserRoleUserNotFound() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        Role newRole = Role.SUPPORTUSER;

        when(userService.updateUserRole(email, newRole))
                .thenThrow(new IllegalArgumentException("User not found with email: " + email));

        String requestBody = """
            {
                "role": "SUPPORTUSER"
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(userService).updateUserRole(email, newRole);
    }

    @Test
    @WithMockUser(roles = "ADMINUSER")
    void testUpdateUserRoleInvalidRequest() throws Exception {
        // Given - missing role field
        String email = "user@example.com";
        String requestBody = """
            {
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserRole(anyString(), any(Role.class));
    }

    @Test
    @WithMockUser(roles = "SUPPORTUSER")
    void testUpdateUserRoleAccessDeniedForSupportUser() throws Exception {
        // Given
        String email = "user@example.com";
        String requestBody = """
            {
                "role": "SUPPORTUSER"
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUserRole(anyString(), any(Role.class));
    }

    @Test
    @WithMockUser(roles = "ENDUSER")
    void testUpdateUserRoleAccessDeniedForEndUser() throws Exception {
        // Given
        String email = "user@example.com";
        String requestBody = """
            {
                "role": "SUPPORTUSER"
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUserRole(anyString(), any(Role.class));
    }

    @Test
    void testUpdateUserRoleUnauthorized() throws Exception {
        // Given
        String email = "user@example.com";
        String requestBody = """
            {
                "role": "SUPPORTUSER"
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUserRole(anyString(), any(Role.class));
    }

    @Test
    @WithMockUser(roles = "ADMINUSER")
    void testUpdateUserRoleWithSpecialCharactersInEmail() throws Exception {
        // Given
        String email = "user+test@example.com";
        Role newRole = Role.SUPPORTUSER;
        UserResponse updatedUser = new UserResponse(email, "testuser", newRole);

        when(userService.updateUserRole(email, newRole)).thenReturn(updatedUser);

        String requestBody = """
            {
                "role": "SUPPORTUSER"
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{mail}/role", email)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value(email))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("SUPPORTUSER"));

        verify(userService).updateUserRole(email, newRole);
    }
}