package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.dto.SupportTicketUpdateRequest;
import de.bachelorarbeit.ticketsystem.dto.CloseTicketRequest;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
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
 * Comprehensive tests for unknown ID error handling with custom 404 error messages.
 * Tests that unknown IDs lead to appropriate 404 responses with German error messages.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
public class UnknownIdErrorHandlingTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

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
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        UserAccount endUser = new UserAccount("enduser", "enduser@test.com", 
                passwordEncoder.encode("password"), Role.ENDUSER);
        userRepository.save(endUser);

        UserAccount supportUser = new UserAccount("support", "support@test.com", 
                passwordEncoder.encode("password"), Role.SUPPORTUSER);
        userRepository.save(supportUser);

        UserAccount adminUser = new UserAccount("admin", "admin@test.com", 
                passwordEncoder.encode("password"), Role.ADMINUSER);
        userRepository.save(adminUser);
    }

    // ========== TICKET CONTROLLER 404 TESTS ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetTicketById_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing GET /api/tickets/{id} with unknown ID");

        mockMvc.perform(get("/api/tickets/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Unknown ticket ID correctly returned 404 with German message");
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetMyTicketById_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing GET /api/tickets/my/{id} with unknown ID");

        mockMvc.perform(get("/api/tickets/my/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Unknown ticket ID in my tickets correctly returned 404 with German message");
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetTicketComments_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing GET /api/tickets/{id}/comments with unknown ID");

        mockMvc.perform(get("/api/tickets/999999/comments"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Unknown ticket ID for comments correctly returned 404 with German message");
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testCreateTicketComment_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing POST /api/tickets/{id}/comments with unknown ID");

        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("Test comment on non-existent ticket");

        mockMvc.perform(post("/api/tickets/999999/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Creating comment on unknown ticket ID correctly returned 404 with German message");
    }

    // ========== SUPPORT TICKET CONTROLLER 404 TESTS ==========

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testAssignTicket_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing POST /api/support/tickets/{id}/assign with unknown ID");

        mockMvc.perform(post("/api/support/tickets/999999/assign"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Assigning unknown ticket ID correctly returned 404 with German message");
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testReleaseTicket_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing POST /api/support/tickets/{id}/release with unknown ID");

        mockMvc.perform(post("/api/support/tickets/999999/release"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Releasing unknown ticket ID correctly returned 404 with German message");
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testUpdateTicket_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing PATCH /api/support/tickets/{id} with unknown ID");

        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.IN_PROGRESS);

        mockMvc.perform(patch("/api/support/tickets/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Updating unknown ticket ID correctly returned 404 with German message");
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testCloseTicket_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing POST /api/support/tickets/{id}/close with unknown ID");

        CloseTicketRequest request = new CloseTicketRequest();
        request.setComment("Closing non-existent ticket");

        mockMvc.perform(post("/api/support/tickets/999999/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Closing unknown ticket ID correctly returned 404 with German message");
    }

    // ========== ADMIN CONTROLLER 404 TESTS ==========

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testUpdateUserRole_UnknownEmail_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing PATCH /api/admin/users/{email}/role with unknown email");

        mockMvc.perform(patch("/api/admin/users/unknown@test.com/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"SUPPORTUSER\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Benutzer nicht gefunden"));

        System.out.println("[DEBUG_LOG] Unknown user email correctly returned 404 with German message");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testDeleteTicket_UnknownId_Returns404WithGermanMessage() throws Exception {
        System.out.println("[DEBUG_LOG] Testing DELETE /api/admin/tickets/{id} with unknown ID");

        mockMvc.perform(delete("/api/admin/tickets/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Deleting unknown ticket ID correctly returned 404 with German message");
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testMultipleUnknownIds_ConsistentErrorMessages() throws Exception {
        System.out.println("[DEBUG_LOG] Testing multiple unknown IDs for consistent error messages");

        // Test different unknown IDs
        Long[] unknownIds = {999999L, 123456L, 0L, -1L};

        for (Long unknownId : unknownIds) {
            mockMvc.perform(get("/api/tickets/" + unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));
        }

        System.out.println("[DEBUG_LOG] All unknown IDs consistently returned 404 with German message");
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testUnknownIdWithDifferentHttpMethods() throws Exception {
        System.out.println("[DEBUG_LOG] Testing unknown ID with different HTTP methods");

        Long unknownId = 999999L;

        // GET request
        mockMvc.perform(get("/api/tickets/" + unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        // POST request (assign)
        mockMvc.perform(post("/api/support/tickets/" + unknownId + "/assign"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        // PATCH request (update)
        SupportTicketUpdateRequest updateRequest = new SupportTicketUpdateRequest();
        updateRequest.setTicketState(TicketState.IN_PROGRESS);

        mockMvc.perform(patch("/api/support/tickets/" + unknownId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Unknown ID consistently handled across different HTTP methods");
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testLargeUnknownId_Returns404() throws Exception {
        System.out.println("[DEBUG_LOG] Testing very large unknown ID");

        Long veryLargeId = Long.MAX_VALUE;

        mockMvc.perform(get("/api/tickets/" + veryLargeId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ticket nicht gefunden"));

        System.out.println("[DEBUG_LOG] Very large unknown ID correctly returned 404");
    }

    // ========== POSITIVE CASE VERIFICATION ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testValidId_AfterCreatingTicket_ReturnsSuccess() throws Exception {
        System.out.println("[DEBUG_LOG] Testing valid ID after creating a ticket (positive case)");

        // First create a ticket to get a valid ID
        String createTicketJson = """
            {
                "title": "Test Ticket",
                "description": "Test Description",
                "ticketCategory": "HARDWARE"
            }
            """;

        String response = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createTicketJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ticket ID from response
        Long ticketId = objectMapper.readTree(response).get("ticketId").asLong();

        // Now test that the valid ID works
        mockMvc.perform(get("/api/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andExpect(jsonPath("$.title").value("Test Ticket"));

        System.out.println("[DEBUG_LOG] Valid ticket ID correctly returned 200 with ticket data");
    }
}