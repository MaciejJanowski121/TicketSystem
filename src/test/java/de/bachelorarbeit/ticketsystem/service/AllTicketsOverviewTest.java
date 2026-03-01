package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketListItemResponse;
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
 * Tests for US7 "All Tickets Overview & Filters" functionality.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AllTicketsOverviewTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private UserAccount testUser;
    private UserAccount supportUser;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser = new UserAccount("testuser", "test@example.com", "hashedpassword", Role.ENDUSER);
        testUser = userRepository.save(testUser);

        supportUser = new UserAccount("supportuser", "support@example.com", "hashedpassword", Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);
    }

    @Test
    void testGetAllTicketsWithoutFilters() {
        // Create test tickets with different states and categories
        Ticket ticket1 = new Ticket("Hardware Issue", "Computer not working", TicketCategory.HARDWARE, testUser);
        ticket1.setTicketState(TicketState.UNASSIGNED);
        ticketRepository.save(ticket1);

        Ticket ticket2 = new Ticket("Network Problem", "Internet connection issues", TicketCategory.NETWORK, testUser);
        ticket2.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(ticket2);

        Ticket ticket3 = new Ticket("Account Issue", "Password reset needed", TicketCategory.ACCOUNT_MANAGEMENT, testUser);
        ticket3.setTicketState(TicketState.CLOSED);
        ticketRepository.save(ticket3);

        // Create authentication for support user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Get all tickets without filters
        List<TicketListItemResponse> tickets = ticketService.getAllTickets(null, null, null, "updateDate", "DESC", auth);

        // Verify results
        assertEquals(3, tickets.size());

        // Verify that creator username is included
        for (TicketListItemResponse ticket : tickets) {
            assertNotNull(ticket.getCreatorUsername());
            assertEquals(testUser.getUsername(), ticket.getCreatorUsername());
        }

        System.out.println("[DEBUG_LOG] Successfully retrieved all tickets without filters");
    }

    @Test
    void testGetAllTicketsWithStateFilter() {
        // Create test tickets with different states
        Ticket ticket1 = new Ticket("Ticket 1", "Description 1", TicketCategory.HARDWARE, testUser);
        ticket1.setTicketState(TicketState.UNASSIGNED);
        ticketRepository.save(ticket1);

        Ticket ticket2 = new Ticket("Ticket 2", "Description 2", TicketCategory.NETWORK, testUser);
        ticket2.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(ticket2);

        Ticket ticket3 = new Ticket("Ticket 3", "Description 3", TicketCategory.OTHER, testUser);
        ticket3.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(ticket3);

        // Create authentication for end user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Get tickets filtered by IN_PROGRESS state
        List<TicketListItemResponse> tickets = ticketService.getAllTickets(null, TicketState.IN_PROGRESS, null, "updateDate", "DESC", auth);

        // Verify results
        assertEquals(2, tickets.size());
        for (TicketListItemResponse ticket : tickets) {
            assertEquals(TicketState.IN_PROGRESS, ticket.getTicketState());
            assertEquals(testUser.getUsername(), ticket.getCreatorUsername());
        }

        System.out.println("[DEBUG_LOG] Successfully filtered tickets by state");
    }

    @Test
    void testGetAllTicketsWithCategoryFilter() {
        // Create test tickets with different categories
        Ticket ticket1 = new Ticket("Hardware Issue", "Description 1", TicketCategory.HARDWARE, testUser);
        ticketRepository.save(ticket1);

        Ticket ticket2 = new Ticket("Network Issue", "Description 2", TicketCategory.NETWORK, testUser);
        ticketRepository.save(ticket2);

        Ticket ticket3 = new Ticket("Another Hardware Issue", "Description 3", TicketCategory.HARDWARE, testUser);
        ticketRepository.save(ticket3);

        // Create authentication for admin user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMINUSER"))
        );

        // Create admin user for authentication
        UserAccount adminUser = new UserAccount("admin", "admin@example.com", "password", Role.ADMINUSER);
        userRepository.save(adminUser);

        // Get tickets filtered by HARDWARE category
        List<TicketListItemResponse> tickets = ticketService.getAllTickets(null, null, TicketCategory.HARDWARE, "updateDate", "DESC", auth);

        // Verify results
        assertEquals(2, tickets.size());
        for (TicketListItemResponse ticket : tickets) {
            assertEquals(TicketCategory.HARDWARE, ticket.getTicketCategory());
            assertEquals(testUser.getUsername(), ticket.getCreatorUsername());
        }

        System.out.println("[DEBUG_LOG] Successfully filtered tickets by category");
    }

    @Test
    void testGetAllTicketsWithBothFilters() {
        // Create test tickets with different states and categories
        Ticket ticket1 = new Ticket("Hardware Issue", "Description 1", TicketCategory.HARDWARE, testUser);
        ticket1.setTicketState(TicketState.UNASSIGNED);
        ticketRepository.save(ticket1);

        Ticket ticket2 = new Ticket("Network Issue", "Description 2", TicketCategory.NETWORK, testUser);
        ticket2.setTicketState(TicketState.UNASSIGNED);
        ticketRepository.save(ticket2);

        Ticket ticket3 = new Ticket("Hardware Problem", "Description 3", TicketCategory.HARDWARE, testUser);
        ticket3.setTicketState(TicketState.IN_PROGRESS);
        ticketRepository.save(ticket3);

        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Get tickets filtered by both UNASSIGNED state and HARDWARE category
        List<TicketListItemResponse> tickets = ticketService.getAllTickets(null, TicketState.UNASSIGNED, TicketCategory.HARDWARE, "updateDate", "DESC", auth);

        // Verify results
        assertEquals(1, tickets.size());
        TicketListItemResponse ticket = tickets.get(0);
        assertEquals(TicketState.UNASSIGNED, ticket.getTicketState());
        assertEquals(TicketCategory.HARDWARE, ticket.getTicketCategory());
        assertEquals(testUser.getUsername(), ticket.getCreatorUsername());

        System.out.println("[DEBUG_LOG] Successfully filtered tickets by both state and category");
    }
}
