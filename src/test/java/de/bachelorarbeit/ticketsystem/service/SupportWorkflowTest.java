package de.bachelorarbeit.ticketsystem.service;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for support workflow functionality.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SupportWorkflowTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private UserAccount endUser;
    private UserAccount supportUser;
    private UserAccount adminUser;
    private UserAccount otherSupportUser;
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

        otherSupportUser = new UserAccount("othersupport", "othersupport@example.com", "password", Role.SUPPORTUSER);
        otherSupportUser = userRepository.save(otherSupportUser);

        // Create test ticket
        testTicket = new Ticket("Test Support Ticket", "Test Description", TicketCategory.HARDWARE, endUser);
        testTicket = ticketRepository.save(testTicket);

        System.out.println("[DEBUG_LOG] Created test ticket with ID: " + testTicket.getTicketId());
    }

    @Test
    void testEndUserCannotAccessSupportEndpoints() {
        Authentication endUserAuth = new UsernamePasswordAuthenticationToken(
                endUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Test getMySupportTickets
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.getMySupportTickets(null, null, null, "updateDate", "DESC", endUserAuth);
        });
        assertTrue(exception.getMessage().contains("Only support users can access"));
        System.out.println("[DEBUG_LOG] End user correctly denied access to support endpoints");
    }

    @Test
    void testSupportUserCanAssignTicket() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket to support user
        TicketResponse response = ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        assertNotNull(response);
        assertEquals(testTicket.getTicketId(), response.getTicketId());
        assertEquals(TicketState.IN_PROGRESS, response.getTicketState());
        assertEquals(supportUser.getUsername(), response.getAssignedSupport());

        System.out.println("[DEBUG_LOG] Support user successfully assigned ticket");
    }

    @Test
    void testSupportUserCannotTakeOverAssignedTicket() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        Authentication otherSupportAuth = new UsernamePasswordAuthenticationToken(
                otherSupportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // First support user assigns ticket
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Second support user tries to take over - should fail
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), otherSupportAuth);
        });
        assertEquals("Ticket already assigned", exception.getMessage());

        System.out.println("[DEBUG_LOG] Support user correctly prevented from taking over assigned ticket");
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
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Admin takes over - should succeed
        TicketResponse response = ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), adminAuth);

        assertNotNull(response);
        assertEquals(adminUser.getUsername(), response.getAssignedSupport());

        System.out.println("[DEBUG_LOG] Admin successfully took over assigned ticket");
    }

    @Test
    void testCannotAssignClosedTicket() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Close the ticket first
        testTicket.setTicketState(TicketState.CLOSED);
        testTicket.setClosedDate(java.time.Instant.now());
        ticketRepository.save(testTicket);

        // Try to assign closed ticket - should fail
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);
        });
        assertEquals("Ticket is closed", exception.getMessage());

        System.out.println("[DEBUG_LOG] Correctly prevented assignment of closed ticket");
    }

    @Test
    void testReleaseTicket() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Release ticket
        TicketResponse response = ticketService.releaseTicket(testTicket.getTicketId(), supportAuth);

        assertNotNull(response);
        assertEquals(TicketState.UNASSIGNED, response.getTicketState());
        assertNull(response.getAssignedSupport());

        System.out.println("[DEBUG_LOG] Support user successfully released ticket");
    }

    @Test
    void testCannotReleaseUnassignedTicket() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Try to release unassigned ticket - should fail
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.releaseTicket(testTicket.getTicketId(), supportAuth);
        });
        assertEquals("Ticket is not assigned", exception.getMessage());

        System.out.println("[DEBUG_LOG] Correctly prevented release of unassigned ticket");
    }

    @Test
    void testUpdateTicketState() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Update ticket to closed
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.CLOSED);

        TicketResponse response = ticketService.updateSupportTicket(testTicket.getTicketId(), request, supportAuth);

        assertNotNull(response);
        assertEquals(TicketState.CLOSED, response.getTicketState());
        assertNotNull(response.getClosedDate());

        System.out.println("[DEBUG_LOG] Support user successfully updated ticket state to CLOSED");
    }

    @Test
    void testUpdateTicketCategory() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Update ticket category
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketCategory(TicketCategory.NETWORK);

        TicketResponse response = ticketService.updateSupportTicket(testTicket.getTicketId(), request, supportAuth);

        assertNotNull(response);
        assertEquals(TicketCategory.NETWORK, response.getTicketCategory());

        System.out.println("[DEBUG_LOG] Support user successfully updated ticket category");
    }

    @Test
    void testCloseTicketShortcut() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Close ticket using shortcut method
        TicketResponse response = ticketService.closeTicket(testTicket.getTicketId(), supportAuth);

        assertNotNull(response);
        assertEquals(TicketState.CLOSED, response.getTicketState());
        assertNotNull(response.getClosedDate());

        System.out.println("[DEBUG_LOG] Support user successfully closed ticket using shortcut");
    }

    @Test
    void testGetMySupportTickets() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Initially no tickets assigned
        List<TicketListItemResponse> tickets = ticketService.getMySupportTickets(
                null, null, null, "updateDate", "DESC", supportAuth);
        assertEquals(0, tickets.size());

        // Assign ticket
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Now should have one ticket
        tickets = ticketService.getMySupportTickets(
                null, null, null, "updateDate", "DESC", supportAuth);
        assertEquals(1, tickets.size());
        assertEquals(testTicket.getTicketId(), tickets.get(0).getTicketId());

        System.out.println("[DEBUG_LOG] Support user successfully retrieved assigned tickets");
    }

    @Test
    void testCannotUpdateUnassignedTicket() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Try to update unassigned ticket - should fail
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.CLOSED);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.updateSupportTicket(testTicket.getTicketId(), request, supportAuth);
        });
        assertTrue(exception.getMessage().contains("You can only update tickets assigned to you"));

        System.out.println("[DEBUG_LOG] Correctly prevented update of unassigned ticket");
    }
}