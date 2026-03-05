package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.UpdateUserRoleRequest;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserAccount endUser;
    private UserAccount supportUser;
    private UserAccount adminUser;

    @BeforeEach
    void setUp() {
        // Setup MockMvc with security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clean up
        userRepository.deleteAll();

        // Create test users
        endUser = new UserAccount("enduser", "enduser@test.com", "hash", Role.ENDUSER);
        endUser = userRepository.save(endUser);

        supportUser = new UserAccount("support", "support@test.com", "hash", Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);

        adminUser = new UserAccount("admin", "admin@test.com", "hash", Role.ADMINUSER);
        adminUser = userRepository.save(adminUser);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testGetAllUsersAsAdmin_Success() throws Exception {
        System.out.println("[DEBUG_LOG] Testing GET /api/admin/users as admin user");

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.mail == 'enduser@test.com')].role").value("ENDUSER"))
                .andExpect(jsonPath("$[?(@.mail == 'support@test.com')].role").value("SUPPORTUSER"))
                .andExpect(jsonPath("$[?(@.mail == 'admin@test.com')].role").value("ADMINUSER"));

        System.out.println("[DEBUG_LOG] Admin successfully retrieved all users");
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllUsersAsEndUser_Forbidden() throws Exception {
        System.out.println("[DEBUG_LOG] Testing GET /api/admin/users as end user - should be forbidden");

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        System.out.println("[DEBUG_LOG] End user correctly denied access to user list");
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testGetAllUsersAsSupport_Forbidden() throws Exception {
        System.out.println("[DEBUG_LOG] Testing GET /api/admin/users as support user - should be forbidden");

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        System.out.println("[DEBUG_LOG] Support user correctly denied access to user list");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testUpdateUserRoleAsAdmin_Success() throws Exception {
        System.out.println("[DEBUG_LOG] Testing PATCH /api/admin/users/{mail}/role as admin user");

        // Verify initial role
        assertEquals(Role.ENDUSER, endUser.getRole());

        // Create update request
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.SUPPORTUSER);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/admin/users/{mail}/role", endUser.getMail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mail").value(endUser.getMail()))
                .andExpect(jsonPath("$.username").value(endUser.getUsername()))
                .andExpect(jsonPath("$.role").value("SUPPORTUSER"));

        // Verify role was updated in database
        UserAccount updatedUser = userRepository.findByMail(endUser.getMail()).get();
        assertEquals(Role.SUPPORTUSER, updatedUser.getRole());

        System.out.println("[DEBUG_LOG] Admin successfully updated user role from ENDUSER to SUPPORTUSER");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testUpdateUserRoleToAdmin_Success() throws Exception {
        System.out.println("[DEBUG_LOG] Testing updating user role to ADMINUSER");

        // Create update request to make support user an admin
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.ADMINUSER);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/admin/users/{mail}/role", supportUser.getMail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMINUSER"));

        // Verify role was updated in database
        UserAccount updatedUser = userRepository.findByMail(supportUser.getMail()).get();
        assertEquals(Role.ADMINUSER, updatedUser.getRole());

        System.out.println("[DEBUG_LOG] Admin successfully promoted support user to admin");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testUpdateNonExistentUser_NotFound() throws Exception {
        System.out.println("[DEBUG_LOG] Testing PATCH /api/admin/users/{mail}/role for non-existent user");

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.SUPPORTUSER);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/admin/users/{mail}/role", "nonexistent@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User not found"));

        System.out.println("[DEBUG_LOG] Non-existent user correctly handled with 404");
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testUpdateUserRoleAsEndUser_Forbidden() throws Exception {
        System.out.println("[DEBUG_LOG] Testing PATCH /api/admin/users/{mail}/role as end user - should be forbidden");

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.SUPPORTUSER);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/admin/users/{mail}/role", supportUser.getMail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        // Verify role was not changed
        UserAccount unchangedUser = userRepository.findByMail(supportUser.getMail()).get();
        assertEquals(Role.SUPPORTUSER, unchangedUser.getRole());

        System.out.println("[DEBUG_LOG] End user correctly denied access to role update");
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testUpdateUserRoleAsSupport_Forbidden() throws Exception {
        System.out.println("[DEBUG_LOG] Testing PATCH /api/admin/users/{mail}/role as support user - should be forbidden");

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.ADMINUSER);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/admin/users/{mail}/role", endUser.getMail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        // Verify role was not changed
        UserAccount unchangedUser = userRepository.findByMail(endUser.getMail()).get();
        assertEquals(Role.ENDUSER, unchangedUser.getRole());

        System.out.println("[DEBUG_LOG] Support user correctly denied access to role update");
    }

    @Test
    void testUpdateUserRoleWithoutAuthentication_Unauthorized() throws Exception {
        System.out.println("[DEBUG_LOG] Testing PATCH /api/admin/users/{mail}/role without authentication");

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.SUPPORTUSER);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/admin/users/{mail}/role", endUser.getMail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());

        // Verify role was not changed
        UserAccount unchangedUser = userRepository.findByMail(endUser.getMail()).get();
        assertEquals(Role.ENDUSER, unchangedUser.getRole());

        System.out.println("[DEBUG_LOG] Unauthenticated request correctly denied");
    }
}
