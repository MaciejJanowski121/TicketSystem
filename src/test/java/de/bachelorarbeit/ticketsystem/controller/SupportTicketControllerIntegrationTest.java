package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.CloseTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.SupportTicketUpdateRequest;
import de.bachelorarbeit.ticketsystem.model.entity.*;
import de.bachelorarbeit.ticketsystem.repository.*;
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

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SupportTicketController endpoints.
 * Tests HTTP endpoints with proper authentication and authorization for support workflow.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
public class SupportTicketControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SupportTicketAssignmentRepository supportTicketAssignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private UserAccount endUser;
    private UserAccount supportUser;
    private UserAccount adminUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up
        supportTicketAssignmentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        endUser = new UserAccount("enduser", "enduser@test.com", 
                passwordEncoder.encode("password"), Role.ENDUSER);
        endUser = userRepository.save(endUser);

        supportUser = new UserAccount("support", "support@test.com", 
                passwordEncoder.encode("password"), Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);

        adminUser = new UserAccount("admin", "admin@test.com", 
                passwordEncoder.encode("password"), Role.ADMINUSER);
        adminUser = userRepository.save(adminUser);

        // Create test ticket
        testTicket = new Ticket();
        testTicket.setTitle("Test Ticket");
        testTicket.setDescription("Test Description");
        testTicket.setTicketState(TicketState.UNASSIGNED);
        testTicket.setTicketCategory(TicketCategory.HARDWARE);
        testTicket.setEndUser(endUser);
        testTicket.setCreateDate(Instant.now());
        testTicket.setUpdateDate(Instant.now());
        testTicket = ticketRepository.save(testTicket);
    }

    // ========== GET MY SUPPORT TICKETS TESTS ==========

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testGetMySupportTickets_Success_SupportUser() throws Exception {
        mockMvc.perform(get("/api/support/tickets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testGetMySupportTickets_Success_AdminUser() throws Exception {
        mockMvc.perform(get("/api/support/tickets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testGetMySupportTickets_WithFilters() throws Exception {
        mockMvc.perform(get("/api/support/tickets/my")
                .param("search", "test")
                .param("state", "UNASSIGNED")
                .param("category", "HARDWARE")
                .param("sort", "createDate")
                .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetMySupportTickets_Forbidden_EndUser() throws Exception {
        mockMvc.perform(get("/api/support/tickets/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMySupportTickets_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(get("/api/support/tickets/my"))
                .andExpect(status().isUnauthorized());
    }

    // ========== ASSIGN TICKET TESTS ==========

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testAssignTicket_Success_SupportUser() throws Exception {
        mockMvc.perform(post("/api/support/tickets/{ticketId}/assign", testTicket.getTicketId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(testTicket.getTicketId()))
                .andExpect(jsonPath("$.ticketState").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testAssignTicket_Success_AdminUser() throws Exception {
        mockMvc.perform(post("/api/support/tickets/{ticketId}/assign", testTicket.getTicketId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(testTicket.getTicketId()))
                .andExpect(jsonPath("$.ticketState").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testAssignTicket_NotFound() throws Exception {
        mockMvc.perform(post("/api/support/tickets/999/assign"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testAssignTicket_Forbidden_EndUser() throws Exception {
        mockMvc.perform(post("/api/support/tickets/{ticketId}/assign", testTicket.getTicketId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAssignTicket_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(post("/api/support/tickets/{ticketId}/assign", testTicket.getTicketId()))
                .andExpect(status().isUnauthorized());
    }

    // ========== RELEASE TICKET TESTS ==========

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testReleaseTicket_Success() throws Exception {
        // First assign the ticket
        testTicket.setAssignedSupportUser(supportUser);
        testTicket.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(testTicket);

        // Create assignment
        SupportTicketAssignment assignment = new SupportTicketAssignment(testTicket, supportUser);
        supportTicketAssignmentRepository.save(assignment);

        mockMvc.perform(post("/api/support/tickets/{ticketId}/release", testTicket.getTicketId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(testTicket.getTicketId()))
                .andExpect(jsonPath("$.ticketState").value("UNASSIGNED"));
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testReleaseTicket_NotFound() throws Exception {
        mockMvc.perform(post("/api/support/tickets/999/release"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testReleaseTicket_Forbidden_EndUser() throws Exception {
        mockMvc.perform(post("/api/support/tickets/{ticketId}/release", testTicket.getTicketId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testReleaseTicket_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(post("/api/support/tickets/{ticketId}/release", testTicket.getTicketId()))
                .andExpect(status().isUnauthorized());
    }

    // ========== UPDATE TICKET TESTS ==========

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testUpdateTicket_Success_StateOnly() throws Exception {
        // Assign ticket first
        testTicket.setAssignedSupportUser(supportUser);
        testTicket.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(testTicket);

        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.IN_PROGRESS);

        mockMvc.perform(patch("/api/support/tickets/{ticketId}", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(testTicket.getTicketId()));
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testUpdateTicket_Success_CategoryOnly() throws Exception {
        // Assign ticket first
        testTicket.setAssignedSupportUser(supportUser);
        testTicket.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(testTicket);

        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketCategory(TicketCategory.NETWORK);

        mockMvc.perform(patch("/api/support/tickets/{ticketId}", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(testTicket.getTicketId()))
                .andExpect(jsonPath("$.ticketCategory").value("NETWORK"));
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testUpdateTicket_Success_StateAndCategory() throws Exception {
        // Assign ticket first
        testTicket.setAssignedSupportUser(supportUser);
        testTicket.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(testTicket);

        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.IN_PROGRESS);
        request.setTicketCategory(TicketCategory.PROGRAMS_TOOLS);

        mockMvc.perform(patch("/api/support/tickets/{ticketId}", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(testTicket.getTicketId()))
                .andExpect(jsonPath("$.ticketCategory").value("PROGRAMS_TOOLS"));
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testUpdateTicket_NotFound() throws Exception {
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.IN_PROGRESS);

        mockMvc.perform(patch("/api/support/tickets/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testUpdateTicket_Forbidden_EndUser() throws Exception {
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.IN_PROGRESS);

        mockMvc.perform(patch("/api/support/tickets/{ticketId}", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateTicket_Unauthorized_NoAuth() throws Exception {
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.IN_PROGRESS);

        mockMvc.perform(patch("/api/support/tickets/{ticketId}", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ========== CLOSE TICKET TESTS ==========

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testCloseTicket_Success() throws Exception {
        // Assign ticket first
        testTicket.setAssignedSupportUser(supportUser);
        testTicket.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(testTicket);

        CloseTicketRequest request = new CloseTicketRequest();
        request.setComment("Ticket resolved successfully");

        mockMvc.perform(post("/api/support/tickets/{ticketId}/close", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(testTicket.getTicketId()))
                .andExpect(jsonPath("$.ticketState").value("CLOSED"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testCloseTicket_Success_AdminUser() throws Exception {
        // Assign ticket first
        testTicket.setAssignedSupportUser(adminUser);
        testTicket.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(testTicket);

        CloseTicketRequest request = new CloseTicketRequest();
        request.setComment("Ticket closed by admin");

        mockMvc.perform(post("/api/support/tickets/{ticketId}/close", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(testTicket.getTicketId()))
                .andExpect(jsonPath("$.ticketState").value("CLOSED"));
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testCloseTicket_ValidationError_EmptyComment() throws Exception {
        CloseTicketRequest request = new CloseTicketRequest();
        request.setComment(""); // Empty comment

        mockMvc.perform(post("/api/support/tickets/{ticketId}/close", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testCloseTicket_NotFound() throws Exception {
        CloseTicketRequest request = new CloseTicketRequest();
        request.setComment("Ticket resolved");

        mockMvc.perform(post("/api/support/tickets/999/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testCloseTicket_Forbidden_EndUser() throws Exception {
        CloseTicketRequest request = new CloseTicketRequest();
        request.setComment("Ticket resolved");

        mockMvc.perform(post("/api/support/tickets/{ticketId}/close", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCloseTicket_Unauthorized_NoAuth() throws Exception {
        CloseTicketRequest request = new CloseTicketRequest();
        request.setComment("Ticket resolved");

        mockMvc.perform(post("/api/support/tickets/{ticketId}/close", testTicket.getTicketId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}