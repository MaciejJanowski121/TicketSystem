package de.bachelorarbeit.ticketsystem;

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
public class UserTicketDuplicateKeyTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserTicketRepository userTicketRepository;

    private UserAccount endUser;
    private UserAccount supportUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Clean up
        userTicketRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        endUser = new UserAccount("enduser", "maciej@hotmail.com", "hash", Role.ENDUSER);
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
    void testNoDuplicateKeyExceptionOnMultipleAccess() {
        System.out.println("[DEBUG_LOG] Testing no duplicate key exception on multiple access");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "maciej@hotmail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // First access - should create UserTicket record
        TicketResponse response1 = ticketService.getMyTicketById(testTicket.getTicketId(), auth);
        assertNotNull(response1, "Should get ticket details on first access");
        System.out.println("[DEBUG_LOG] First access successful");

        // Simulate support updating the ticket (changing updateDate)
        testTicket.setUpdateDate(Instant.now().minusSeconds(900)); // 15 minutes ago (newer)
        testTicket = ticketRepository.save(testTicket);
        System.out.println("[DEBUG_LOG] Support updated ticket, updateDate changed");

        // Second access - should NOT throw duplicate key exception
        assertDoesNotThrow(() -> {
            TicketResponse response2 = ticketService.getMyTicketById(testTicket.getTicketId(), auth);
            assertNotNull(response2, "Should get ticket details on second access");
            System.out.println("[DEBUG_LOG] Second access successful - no duplicate key exception");
        }, "Should not throw duplicate key exception on second access");

        // Third access - should still work
        assertDoesNotThrow(() -> {
            TicketResponse response3 = ticketService.getMyTicketById(testTicket.getTicketId(), auth);
            assertNotNull(response3, "Should get ticket details on third access");
            System.out.println("[DEBUG_LOG] Third access successful - no duplicate key exception");
        }, "Should not throw duplicate key exception on third access");

        // Verify only one UserTicket record exists
        List<UserTicket> userTickets = userTicketRepository.findByEndUser(endUser);
        assertEquals(1, userTickets.size(), "Should have exactly one UserTicket record");
        System.out.println("[DEBUG_LOG] Verified only one UserTicket record exists");
    }

    @Test
    void testUserTicketCreationAndUpdate() {
        System.out.println("[DEBUG_LOG] Testing UserTicket creation and update with EmbeddedId approach");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "maciej@hotmail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Verify no UserTicket exists initially
        assertFalse(userTicketRepository.findByTicketAndEndUser(testTicket, endUser).isPresent(), 
                   "Should not have UserTicket record initially");

        // First access - should create UserTicket
        TicketResponse response1 = ticketService.getMyTicketById(testTicket.getTicketId(), auth);
        assertNotNull(response1, "Should get ticket details");

        // Verify UserTicket was created
        assertTrue(userTicketRepository.findByTicketAndEndUser(testTicket, endUser).isPresent(), 
                  "Should have UserTicket record after first access");

        UserTicket userTicket1 = userTicketRepository.findByTicketAndEndUser(testTicket, endUser).get();
        Instant firstViewTime = userTicket1.getLastViewed();
        System.out.println("[DEBUG_LOG] UserTicket created with lastViewed: " + firstViewTime);

        // Wait a bit and access again
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Second access - should update existing UserTicket
        TicketResponse response2 = ticketService.getMyTicketById(testTicket.getTicketId(), auth);
        assertNotNull(response2, "Should get ticket details on second access");

        // Verify UserTicket was updated, not duplicated
        UserTicket userTicket2 = userTicketRepository.findByTicketAndEndUser(testTicket, endUser).get();
        Instant secondViewTime = userTicket2.getLastViewed();
        assertTrue(secondViewTime.isAfter(firstViewTime), 
                  "LastViewed should be updated on second access");
        System.out.println("[DEBUG_LOG] UserTicket updated with new lastViewed: " + secondViewTime);

        // Verify still only one record
        List<UserTicket> allUserTickets = userTicketRepository.findByEndUser(endUser);
        assertEquals(1, allUserTickets.size(), "Should still have exactly one UserTicket record");
        System.out.println("[DEBUG_LOG] Verified no duplicate UserTicket records created");
    }
}
