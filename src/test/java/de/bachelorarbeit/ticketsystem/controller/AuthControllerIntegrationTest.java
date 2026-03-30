package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.ChangePasswordRequest;
import de.bachelorarbeit.ticketsystem.dto.LoginRequest;
import de.bachelorarbeit.ticketsystem.dto.RegisterRequest;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController endpoints.
 * Tests HTTP endpoints with proper validation and error handling.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up
        userRepository.deleteAll();
    }

    // ========== REGISTER TESTS ==========

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void testRegister_ValidationError_EmptyUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(""); // Empty username
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_ValidationError_InvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("invalid-email"); // Invalid email format
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_ValidationError_ShortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("123"); // Too short password

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_DuplicateUsername() throws Exception {
        // Create existing user
        UserAccount existingUser = new UserAccount("testuser", "existing@example.com", 
                passwordEncoder.encode("password"), Role.ENDUSER);
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser"); // Duplicate username
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRegister_DuplicateEmail() throws Exception {
        // Create existing user
        UserAccount existingUser = new UserAccount("existinguser", "test@example.com", 
                passwordEncoder.encode("password"), Role.ENDUSER);
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com"); // Duplicate email
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRegister_EmptyBody() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ========== LOGIN TESTS ==========

    @Test
    void testLogin_Success_WithUsername() throws Exception {
        // Create test user
        UserAccount user = new UserAccount("testuser", "test@example.com", 
                passwordEncoder.encode("password123"), Role.ENDUSER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setLogin("testuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLogin_Success_WithEmail() throws Exception {
        // Create test user
        UserAccount user = new UserAccount("testuser", "test@example.com", 
                passwordEncoder.encode("password123"), Role.ENDUSER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setLogin("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLogin_InvalidCredentials_WrongPassword() throws Exception {
        // Create test user
        UserAccount user = new UserAccount("testuser", "test@example.com", 
                passwordEncoder.encode("password123"), Role.ENDUSER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setLogin("testuser");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("nonexistent");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testLogin_ValidationError_EmptyLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin(""); // Empty login
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_ValidationError_EmptyPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("testuser");
        request.setPassword(""); // Empty password

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== CHANGE PASSWORD TESTS ==========

    @Test
    @WithMockUser(username = "testuser", roles = {"ENDUSER"})
    void testChangePassword_Success() throws Exception {
        // Create test user
        UserAccount user = new UserAccount("testuser", "test@example.com", 
                passwordEncoder.encode("oldpassword"), Role.ENDUSER);
        userRepository.save(user);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpassword");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Passwort erfolgreich geändert"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ENDUSER"})
    void testChangePassword_InvalidCurrentPassword() throws Exception {
        // Create test user
        UserAccount user = new UserAccount("testuser", "test@example.com", 
                passwordEncoder.encode("oldpassword"), Role.ENDUSER);
        userRepository.save(user);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongpassword");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ungültiges aktuelles Passwort"))
                .andExpect(jsonPath("$.errors.currentPassword").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ENDUSER"})
    void testChangePassword_PasswordMismatch() throws Exception {
        // Create test user
        UserAccount user = new UserAccount("testuser", "test@example.com", 
                passwordEncoder.encode("oldpassword"), Role.ENDUSER);
        userRepository.save(user);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpassword");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("differentpassword");

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validierung fehlgeschlagen"))
                .andExpect(jsonPath("$.errors.confirmPassword").value("Passwörter stimmen nicht überein"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ENDUSER"})
    void testChangePassword_SameAsCurrentPassword() throws Exception {
        // Create test user
        UserAccount user = new UserAccount("testuser", "test@example.com", 
                passwordEncoder.encode("password123"), Role.ENDUSER);
        userRepository.save(user);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("password123");
        request.setNewPassword("password123"); // Same as current
        request.setConfirmPassword("password123");

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Das neue Passwort muss sich vom aktuellen Passwort unterscheiden"))
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ENDUSER"})
    void testChangePassword_ValidationError_ShortPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpassword");
        request.setNewPassword("123"); // Too short
        request.setConfirmPassword("123");

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void testChangePassword_Unauthorized_NoAuth() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpassword");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "nonexistent", roles = {"ENDUSER"})
    void testChangePassword_UserNotFound() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpassword");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
