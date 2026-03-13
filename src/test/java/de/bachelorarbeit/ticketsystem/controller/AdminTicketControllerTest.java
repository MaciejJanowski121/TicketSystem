package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.model.entity.*;
import de.bachelorarbeit.ticketsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminTicketControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserTicketRepository userTicketRepository;

    @Autowired
    private SupportTicketAssignmentRepository supportTicketAssignmentRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    private UserAccount endUser;
    private UserAccount supportUser;
    private UserAccount adminUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Setup MockMvc with security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clean up
        ticketCommentRepository.deleteAll();
        userTicketRepository.deleteAll();
        supportTicketAssignmentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        endUser = new UserAccount("enduser", "enduser@test.com", "hash", Role.ENDUSER);
        endUser = userRepository.save(endUser);

        supportUser = new UserAccount("support", "support@test.com", "hash", Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);

        adminUser = new UserAccount("admin", "admin@test.com", "hash", Role.ADMINUSER);
        adminUser = userRepository.save(adminUser);

        // Create test ticket
        testTicket = new Ticket();
        testTicket.setTitle("Test Ticket for Deletion");
        testTicket.setDescription("Test Description");
        testTicket.setTicketState(TicketState.IN_PROGRESS);
        testTicket.setTicketCategory(TicketCategory.PROGRAMS_TOOLS);
        testTicket.setEndUser(endUser);
        testTicket.setAssignedSupportUser(supportUser);
        testTicket.setCreateDate(Instant.now().minusSeconds(3600));
        testTicket.setUpdateDate(Instant.now().minusSeconds(1800));
        testTicket = ticketRepository.save(testTicket);

        // Create related data
        UserTicket userTicket = new UserTicket(testTicket, endUser);
        userTicketRepository.save(userTicket);

        SupportTicketAssignment assignment = new SupportTicketAssignment(testTicket, supportUser);
        supportTicketAssignmentRepository.save(assignment);

        TicketComment comment = new TicketComment(testTicket, endUser, "Test comment");
        ticketCommentRepository.save(comment);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testDeleteTicketAsAdmin_Success() throws Exception {
        System.out.println("[DEBUG_LOG] Testing DELETE /api/admin/tickets/{id} as admin user");

        // Verify ticket exists before deletion
        assertTrue(ticketRepository.findById(testTicket.getTicketId()).isPresent(),
                "Ticket should exist before deletion");

        // Perform DELETE request
        mockMvc.perform(delete("/api/admin/tickets/{ticketId}", testTicket.getTicketId()))
                .andExpect(status().isNoContent()); // 204 No Content

        // Verify ticket and related data are deleted
        assertFalse(ticketRepository.findById(testTicket.getTicketId()).isPresent(),
                "Ticket should be deleted");
        assertTrue(userTicketRepository.findByTicket(testTicket).isEmpty(),
                "UserTicket records should be deleted");
        assertFalse(supportTicketAssignmentRepository.findByTicket(testTicket).isPresent(),
                "SupportTicketAssignment should be deleted");
        assertTrue(ticketCommentRepository.findByTicket(testTicket).isEmpty(),
                "TicketComment records should be deleted");

        System.out.println("[DEBUG_LOG] Admin successfully deleted ticket via HTTP endpoint");
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testDeleteTicketAsEndUser_Forbidden() throws Exception {
        System.out.println("[DEBUG_LOG] Testing DELETE /api/admin/tickets/{id} as end user - should be forbidden");

        // Perform DELETE request as end user - should return 403 Forbidden
        mockMvc.perform(delete("/api/admin/tickets/{ticketId}", testTicket.getTicketId()))
                .andExpect(status().isForbidden()); // 403 Forbidden

        // Verify ticket still exists
        assertTrue(ticketRepository.findById(testTicket.getTicketId()).isPresent(),
                "Ticket should still exist after forbidden deletion attempt");

        System.out.println("[DEBUG_LOG] End user correctly denied access via HTTP endpoint");
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testDeleteTicketAsSupport_Forbidden() throws Exception {
        System.out.println("[DEBUG_LOG] Testing DELETE /api/admin/tickets/{id} as support user - should be forbidden");

        // Perform DELETE request as support user - should return 403 Forbidden
        mockMvc.perform(delete("/api/admin/tickets/{ticketId}", testTicket.getTicketId()))
                .andExpect(status().isForbidden()); // 403 Forbidden

        // Verify ticket still exists
        assertTrue(ticketRepository.findById(testTicket.getTicketId()).isPresent(),
                "Ticket should still exist after forbidden deletion attempt");

        System.out.println("[DEBUG_LOG] Support user correctly denied access via HTTP endpoint");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testDeleteNonExistentTicket_NotFound() throws Exception {
        System.out.println("[DEBUG_LOG] Testing DELETE /api/admin/tickets/{id} for non-existent ticket");

        Long nonExistentTicketId = 99999L;

        // Perform DELETE request for non-existent ticket - should return 404 Not Found
        mockMvc.perform(delete("/api/admin/tickets/{ticketId}", nonExistentTicketId))
                .andExpect(status().isNotFound()); // 404 Not Found

        System.out.println("[DEBUG_LOG] Non-existent ticket correctly handled via HTTP endpoint");
    }

    @Test
    void testDeleteTicketWithoutAuthentication_Unauthorized() throws Exception {
        System.out.println("[DEBUG_LOG] Testing DELETE /api/admin/tickets/{id} without authentication");

        // Perform DELETE request without authentication - should return 401 Unauthorized
        mockMvc.perform(delete("/api/admin/tickets/{ticketId}", testTicket.getTicketId()))
                .andExpect(status().isUnauthorized()); // 401 Unauthorized

        // Verify ticket still exists
        assertTrue(ticketRepository.findById(testTicket.getTicketId()).isPresent(),
                "Ticket should still exist after unauthorized deletion attempt");

        System.out.println("[DEBUG_LOG] Unauthenticated request correctly denied via HTTP endpoint");
    }
}
