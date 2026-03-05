package de.bachelorarbeit.ticketsystem;

import de.bachelorarbeit.ticketsystem.dto.TicketListItemResponse;
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

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UnreadFunctionalityTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserTicketRepository userTicketRepository;

    @Autowired
    private SupportTicketAssignmentRepository supportTicketAssignmentRepository;

    private UserAccount endUser;
    private UserAccount supportUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Clean up
        userTicketRepository.deleteAll();
        supportTicketAssignmentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        endUser = new UserAccount("enduser", "enduser@test.com", "hash", Role.ENDUSER);
        endUser = userRepository.save(endUser);

        supportUser = new UserAccount("support", "support@test.com", "hash", Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);

        // Create test ticket
        testTicket = new Ticket();
        testTicket.setTitle("Test Ticket");
        testTicket.setDescription("Test Description");
        testTicket.setTicketState(TicketState.UNASSIGNED);
        testTicket.setTicketCategory(TicketCategory.PROGRAMS_TOOLS);
        testTicket.setEndUser(endUser);
        testTicket.setCreateDate(Instant.now().minusSeconds(3600)); // 1 hour ago
        testTicket.setUpdateDate(Instant.now().minusSeconds(1800)); // 30 minutes ago
        testTicket = ticketRepository.save(testTicket);
    }

    @Test
    void testEndUserUnreadFunctionality() {
        System.out.println("[DEBUG_LOG] Testing ENDUSER unread functionality");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "enduser@test.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Get tickets - should be unread since no UserTicket record exists
        List<TicketResponse> tickets = ticketService.getMyTickets(auth);

        assertFalse(tickets.isEmpty(), "Should have tickets");
        TicketResponse ticket = tickets.get(0);
        assertTrue(ticket.isUnread(), "Ticket should be unread initially");
        System.out.println("[DEBUG_LOG] Initial ticket unread status: " + ticket.isUnread());

        // View the ticket details - this should create/update UserTicket record
        TicketResponse detailResponse = ticketService.getMyTicketById(testTicket.getTicketId(), auth);
        assertNotNull(detailResponse, "Should get ticket details");
        System.out.println("[DEBUG_LOG] Viewed ticket details");

        // Get tickets again - should now be read since we just viewed it
        tickets = ticketService.getMyTickets(auth);
        ticket = tickets.get(0);
        assertFalse(ticket.isUnread(), "Ticket should be read after viewing details");
        System.out.println("[DEBUG_LOG] After viewing details, ticket unread status: " + ticket.isUnread());
    }

    @Test
    void testSupportUserUnreadFunctionality() {
        System.out.println("[DEBUG_LOG] Testing SUPPORT user unread functionality");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "support@test.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket to support user
        testTicket.setAssignedSupport(supportUser);
        testTicket.setTicketState(TicketState.IN_PROGRESS);
        testTicket = ticketRepository.save(testTicket);

        // Create assignment with old lastViewed time
        SupportTicketAssignment assignment = new SupportTicketAssignment(testTicket, supportUser);
        assignment.setLastViewed(Instant.now().minusSeconds(3600)); // 1 hour ago
        supportTicketAssignmentRepository.save(assignment);

        // Get support tickets - should be unread since ticket was updated after lastViewed
        List<TicketListItemResponse> tickets = ticketService.getMySupportTickets(null, null, null, "updateDate", "DESC", auth);

        assertFalse(tickets.isEmpty(), "Should have assigned tickets");
        TicketListItemResponse ticket = tickets.get(0);
        assertTrue(ticket.isUnread(), "Ticket should be unread for support user");
        System.out.println("[DEBUG_LOG] Support ticket unread status: " + ticket.isUnread());

        // View the ticket details - this should update lastViewed
        TicketResponse detailResponse = ticketService.getTicketById(testTicket.getTicketId(), auth);
        assertNotNull(detailResponse, "Should get ticket details");
        System.out.println("[DEBUG_LOG] Support user viewed ticket details");

        // Get tickets again - should now be read
        tickets = ticketService.getMySupportTickets(null, null, null, "updateDate", "DESC", auth);
        ticket = tickets.get(0);
        assertFalse(ticket.isUnread(), "Ticket should be read after support user viewed details");
        System.out.println("[DEBUG_LOG] After support viewing details, ticket unread status: " + ticket.isUnread());
    }

    @Test
    void testUnreadFirstSorting() {
        System.out.println("[DEBUG_LOG] Testing unread-first sorting");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "enduser@test.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create a second ticket
        Ticket ticket2 = new Ticket();
        ticket2.setTitle("Second Ticket");
        ticket2.setDescription("Second Description");
        ticket2.setTicketState(TicketState.UNASSIGNED);
        ticket2.setTicketCategory(TicketCategory.HARDWARE);
        ticket2.setEndUser(endUser);
        ticket2.setCreateDate(Instant.now().minusSeconds(1800)); // 30 minutes ago
        ticket2.setUpdateDate(Instant.now().minusSeconds(900)); // 15 minutes ago (newer)
        ticket2 = ticketRepository.save(ticket2);

        // Create UserTicket for first ticket (mark as read)
        UserTicket userTicket = new UserTicket(testTicket, endUser);
        userTicket.setLastViewed(Instant.now()); // Just viewed
        userTicketRepository.save(userTicket);

        // Get tickets - second ticket should be first (unread), first ticket should be second (read)
        List<TicketResponse> tickets = ticketService.getMyTickets(auth);

        assertEquals(2, tickets.size(), "Should have 2 tickets");

        TicketResponse firstInList = tickets.get(0);
        TicketResponse secondInList = tickets.get(1);

        assertTrue(firstInList.isUnread(), "First ticket in list should be unread");
        assertFalse(secondInList.isUnread(), "Second ticket in list should be read");

        assertEquals("Second Ticket", firstInList.getTitle(), "Unread ticket should be first");
        assertEquals("Test Ticket", secondInList.getTitle(), "Read ticket should be second");

        System.out.println("[DEBUG_LOG] Sorting verified: unread ticket '" + firstInList.getTitle() + "' comes before read ticket '" + secondInList.getTitle() + "'");
    }
}
