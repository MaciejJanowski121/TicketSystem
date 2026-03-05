package de.bachelorarbeit.ticketsystem;

import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.model.entity.*;
import de.bachelorarbeit.ticketsystem.repository.*;
import de.bachelorarbeit.ticketsystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DuplicateKeyIssueTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserTicketRepository userTicketRepository;

    private UserAccount endUser;

    @BeforeEach
    void setUp() {
        // Clean up
        userTicketRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        endUser = new UserAccount("enduser", "enduser@test.com", "hash", Role.ENDUSER);
        endUser = userRepository.save(endUser);
    }

    @Test
    void testCreatedTicketShouldNotBeUnreadForCreator() {
        System.out.println("[DEBUG_LOG] Testing that created tickets are not unread for creator");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "enduser@test.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setTitle("Test Ticket");
        createRequest.setDescription("Test Description");
        createRequest.setTicketCategory(TicketCategory.PROGRAMS_TOOLS);

        TicketResponse createdTicket = ticketService.createTicket(createRequest, auth);
        assertNotNull(createdTicket, "Ticket should be created successfully");
        System.out.println("[DEBUG_LOG] Created ticket with ID: " + createdTicket.getTicketId());

        // Get tickets - should NOT be unread for the creator
        List<TicketResponse> myTickets = ticketService.getMyTickets(auth);
        assertFalse(myTickets.isEmpty(), "Should have tickets");
        TicketResponse ticketInList = myTickets.get(0);
        assertFalse(ticketInList.isUnread(), "Ticket should NOT be unread for the creator");
        System.out.println("[DEBUG_LOG] Ticket unread status for creator: " + ticketInList.isUnread());

        // Verify UserTicket record was created during ticket creation
        List<UserTicket> userTickets = userTicketRepository.findByEndUser(endUser);
        assertEquals(1, userTickets.size(), "Should have exactly one UserTicket record");
        System.out.println("[DEBUG_LOG] UserTicket record was created during ticket creation");
    }

    @Test
    void testIdempotentViewTracking() {
        System.out.println("[DEBUG_LOG] Testing idempotent view tracking");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "enduser@test.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setTitle("Idempotent Test");
        createRequest.setDescription("Test Description");
        createRequest.setTicketCategory(TicketCategory.HARDWARE);

        TicketResponse createdTicket = ticketService.createTicket(createRequest, auth);
        System.out.println("[DEBUG_LOG] Created ticket with ID: " + createdTicket.getTicketId());

        // Open ticket multiple times - should never throw duplicate key exception
        for (int i = 1; i <= 10; i++) {
            final int attempt = i;
            assertDoesNotThrow(() -> {
                TicketResponse openedTicket = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
                assertNotNull(openedTicket, "Should successfully open ticket on attempt " + attempt);
                System.out.println("[DEBUG_LOG] Successfully opened ticket on attempt " + attempt);
            }, "Should not throw duplicate key exception on attempt " + i);
        }

        // Verify still only one UserTicket record
        List<UserTicket> userTickets = userTicketRepository.findByEndUser(endUser);
        assertEquals(1, userTickets.size(), "Should still have exactly one UserTicket record after multiple opens");
        System.out.println("[DEBUG_LOG] Verified still only one UserTicket record after multiple opens");
    }

    @Test
    void testPKConsistency() {
        System.out.println("[DEBUG_LOG] Testing PK consistency - enduser_mail vs username");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "enduser@test.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setTitle("PK Test");
        createRequest.setDescription("Test Description");
        createRequest.setTicketCategory(TicketCategory.NETWORK);

        TicketResponse createdTicket = ticketService.createTicket(createRequest, auth);
        System.out.println("[DEBUG_LOG] Created ticket with ID: " + createdTicket.getTicketId());

        // Verify UserTicket exists and uses the correct endUser (by email)
        Ticket ticket = ticketRepository.findById(createdTicket.getTicketId()).orElse(null);
        assertNotNull(ticket, "Ticket should exist");

        assertTrue(userTicketRepository.findByTicketAndEndUser(ticket, endUser).isPresent(), 
                  "UserTicket should exist for the correct endUser: " + endUser.getMail());

        // Create a fake user with the same username but different email to verify distinction
        UserAccount fakeUser = new UserAccount(endUser.getUsername(), "fake@test.com", "hash", Role.ENDUSER);
        assertFalse(userTicketRepository.findByTicketAndEndUser(ticket, fakeUser).isPresent(), 
                   "UserTicket should NOT exist for different user even with same username");

        System.out.println("[DEBUG_LOG] PK consistency verified - uses email: " + endUser.getMail());
    }
}
