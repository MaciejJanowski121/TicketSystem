package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.ErrorResponse;
import de.bachelorarbeit.ticketsystem.dto.SupportTicketUpdateRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketListItemResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.model.entity.*;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for support ticket controller endpoints.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SupportTicketControllerTest {

    @Autowired
    private SupportTicketController supportTicketController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private UserAccount endUser;
    private UserAccount supportUser;
    private UserAccount adminUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Clean up
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        endUser = new UserAccount("enduser", "enduser@example.com", "password", Role.ENDUSER);
        endUser = userRepository.save(endUser);

        supportUser = new UserAccount("supportuser", "support@example.com", "password", Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);

        adminUser = new UserAccount("adminuser", "admin@example.com", "password", Role.ADMINUSER);
        adminUser = userRepository.save(adminUser);

        // Create test ticket
        testTicket = new Ticket("Test Support Ticket", "Test Description", TicketCategory.HARDWARE, endUser);
        testTicket = ticketRepository.save(testTicket);

        System.out.println("[DEBUG_LOG] Created test ticket with ID: " + testTicket.getTicketId());
    }

    @Test
    void testGetMySupportTicketsSuccess() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        ResponseEntity<?> response = supportTicketController.getMySupportTickets(
                null, null, null, "updateDate", "DESC", supportAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);

        @SuppressWarnings("unchecked")
        List<TicketListItemResponse> tickets = (List<TicketListItemResponse>) response.getBody();
        assertEquals(0, tickets.size()); // No tickets assigned initially

        System.out.println("[DEBUG_LOG] Controller returned OK for getMySupportTickets");
    }

    @Test
    void testGetMySupportTicketsForbiddenForEndUser() {
        Authentication endUserAuth = new UsernamePasswordAuthenticationToken(
                endUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        ResponseEntity<?> response = supportTicketController.getMySupportTickets(
                null, null, null, "updateDate", "DESC", endUserAuth);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Only support users can access"));

        System.out.println("[DEBUG_LOG] Controller correctly returned 403 for end user");
    }

    @Test
    void testAssignTicketSuccess() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        ResponseEntity<?> response = supportTicketController.assignTicket(testTicket.getTicketId(), supportAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TicketResponse);

        TicketResponse ticketResponse = (TicketResponse) response.getBody();
        assertEquals(TicketState.IN_PROGRESS, ticketResponse.getTicketState());
        assertEquals(supportUser.getUsername(), ticketResponse.getAssignedSupport());

        System.out.println("[DEBUG_LOG] Controller successfully assigned ticket");
    }

    @Test
    void testAssignTicketNotFound() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        ResponseEntity<?> response = supportTicketController.assignTicket(999L, supportAuth);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Ticket not found", errorResponse.getMessage());

        System.out.println("[DEBUG_LOG] Controller correctly returned 404 for non-existent ticket");
    }

    @Test
    void testAssignAlreadyAssignedTicketConflict() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                adminUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMINUSER"))
        );

        // First assign to support user
        supportTicketController.assignTicket(testTicket.getTicketId(), supportAuth);

        // Try to assign to same support user again - should return conflict
        ResponseEntity<?> response = supportTicketController.assignTicket(testTicket.getTicketId(), supportAuth);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Ticket already assigned", errorResponse.getMessage());

        System.out.println("[DEBUG_LOG] Controller correctly returned 409 for already assigned ticket");
    }

    @Test
    void testReleaseTicketSuccess() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        supportTicketController.assignTicket(testTicket.getTicketId(), supportAuth);

        // Release ticket
        ResponseEntity<?> response = supportTicketController.releaseTicket(testTicket.getTicketId(), supportAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TicketResponse);

        TicketResponse ticketResponse = (TicketResponse) response.getBody();
        assertEquals(TicketState.UNASSIGNED, ticketResponse.getTicketState());
        assertNull(ticketResponse.getAssignedSupport());

        System.out.println("[DEBUG_LOG] Controller successfully released ticket");
    }

    @Test
    void testUpdateTicketSuccess() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        supportTicketController.assignTicket(testTicket.getTicketId(), supportAuth);

        // Update ticket
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.CLOSED);
        request.setTicketCategory(TicketCategory.NETWORK);

        ResponseEntity<?> response = supportTicketController.updateTicket(testTicket.getTicketId(), request, supportAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TicketResponse);

        TicketResponse ticketResponse = (TicketResponse) response.getBody();
        assertEquals(TicketState.CLOSED, ticketResponse.getTicketState());
        assertEquals(TicketCategory.NETWORK, ticketResponse.getTicketCategory());
        assertNotNull(ticketResponse.getClosedDate());

        System.out.println("[DEBUG_LOG] Controller successfully updated ticket");
    }

    @Test
    void testUpdateTicketConflictForInProgressWithoutAssignment() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign and then release ticket to make it unassigned
        supportTicketController.assignTicket(testTicket.getTicketId(), supportAuth);
        supportTicketController.releaseTicket(testTicket.getTicketId(), supportAuth);

        // Try to set state to IN_PROGRESS without assignment - should fail
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.IN_PROGRESS);

        ResponseEntity<?> response = supportTicketController.updateTicket(testTicket.getTicketId(), request, supportAuth);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);

        System.out.println("[DEBUG_LOG] Controller correctly prevented IN_PROGRESS without assignment");
    }

    @Test
    void testCloseTicketSuccess() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        supportTicketController.assignTicket(testTicket.getTicketId(), supportAuth);

        // Close ticket
        ResponseEntity<?> response = supportTicketController.closeTicket(testTicket.getTicketId(), supportAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TicketResponse);

        TicketResponse ticketResponse = (TicketResponse) response.getBody();
        assertEquals(TicketState.CLOSED, ticketResponse.getTicketState());
        assertNotNull(ticketResponse.getClosedDate());

        System.out.println("[DEBUG_LOG] Controller successfully closed ticket");
    }

    @Test
    void testAdminCanTakeOverAssignedTicket() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                adminUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMINUSER"))
        );

        // Support user assigns ticket
        supportTicketController.assignTicket(testTicket.getTicketId(), supportAuth);

        // Admin takes over - should succeed
        ResponseEntity<?> response = supportTicketController.assignTicket(testTicket.getTicketId(), adminAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TicketResponse);

        TicketResponse ticketResponse = (TicketResponse) response.getBody();
        assertEquals(adminUser.getUsername(), ticketResponse.getAssignedSupport());

        System.out.println("[DEBUG_LOG] Controller allowed admin to take over assigned ticket");
    }
}