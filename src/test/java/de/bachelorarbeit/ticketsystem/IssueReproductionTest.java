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
public class IssueReproductionTest {

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
        endUser = new UserAccount("enduser", "user@mail.com", "hash", Role.ENDUSER);
        endUser = userRepository.save(endUser);
    }

    @Test
    void testIssueScenario_CreateTicketThenOpenImmediately() {
        System.out.println("[DEBUG_LOG] Testing issue scenario: EndUser creates ticket then opens it immediately");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@mail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Step 1: EndUser creates ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setTitle("Test Ticket");
        createRequest.setDescription("Test Description");
        createRequest.setTicketCategory(TicketCategory.PROGRAMS_TOOLS);

        TicketResponse createdTicket = ticketService.createTicket(createRequest, auth);
        assertNotNull(createdTicket, "Ticket should be created successfully");
        System.out.println("[DEBUG_LOG] EndUser created ticket with ID: " + createdTicket.getTicketId());

        // Step 2: Ticket should NOT be unread for the creator (as per business logic)
        List<TicketResponse> myTickets = ticketService.getMyTickets(auth);
        assertFalse(myTickets.isEmpty(), "Should have tickets");
        TicketResponse ticketInList = myTickets.get(0);
        assertFalse(ticketInList.isUnread(), "Ticket should NOT be unread for the creator");
        System.out.println("[DEBUG_LOG] Ticket is not unread for creator: " + !ticketInList.isUnread());

        // Step 3: EndUser opens GET /api/tickets/my/{id} - this should NOT throw 500 duplicate key
        assertDoesNotThrow(() -> {
            TicketResponse openedTicket = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
            assertNotNull(openedTicket, "Should successfully open ticket details");
            System.out.println("[DEBUG_LOG] EndUser successfully opened ticket details - no duplicate key exception");
        }, "Should not throw duplicate key exception when opening ticket details");

        // Step 4: Verify UserTicket record was created properly
        List<UserTicket> userTickets = userTicketRepository.findByEndUser(endUser);
        assertEquals(1, userTickets.size(), "Should have exactly one UserTicket record");
        System.out.println("[DEBUG_LOG] Verified exactly one UserTicket record exists");

        // Step 5: Verify ticket remains as read (same state as before since it was already read for creator)
        myTickets = ticketService.getMyTickets(auth);
        ticketInList = myTickets.get(0);
        assertFalse(ticketInList.isUnread(), "Ticket should remain as read after opening details");
        System.out.println("[DEBUG_LOG] Ticket remains as read: " + !ticketInList.isUnread());
    }

    @Test
    void testMultipleOpensAfterCreation() {
        System.out.println("[DEBUG_LOG] Testing multiple opens after ticket creation");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@mail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setTitle("Multiple Opens Test");
        createRequest.setDescription("Test Description");
        createRequest.setTicketCategory(TicketCategory.HARDWARE);

        TicketResponse createdTicket = ticketService.createTicket(createRequest, auth);
        System.out.println("[DEBUG_LOG] Created ticket with ID: " + createdTicket.getTicketId());

        // Open ticket multiple times - should never throw duplicate key exception
        for (int i = 1; i <= 5; i++) {
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
}
