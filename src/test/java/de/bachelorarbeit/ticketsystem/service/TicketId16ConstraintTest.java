package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.model.entity.*;
import de.bachelorarbeit.ticketsystem.repository.TicketCommentRepository;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to reproduce and fix foreign key constraint violation for ticket ID 16.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketId16ConstraintTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    private UserAccount testUser;

    @BeforeEach
    void setUp() {
        ticketCommentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new UserAccount("testuser", "test@example.com", "hashedpassword", Role.ENDUSER);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateCommentForTicketId16WhenNotExists() {
        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest("Comment for ticket 16");

        // Try to create comment for ticket ID 16 (which doesn't exist)
        System.out.println("[DEBUG_LOG] Attempting to create comment for non-existent ticket ID 16");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicketComment(16L, request, auth);
        });

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Correctly threw IllegalArgumentException: " + exception.getMessage());
    }

    @Test
    void testCreateCommentForTicketId16WhenExists() {
        // Create tickets to get to ID 16 (assuming auto-increment)
        for (int i = 1; i <= 16; i++) {
            Ticket ticket = new Ticket("Test Ticket " + i, "Description " + i, TicketCategory.HARDWARE, testUser);
            ticketRepository.save(ticket);
        }

        // Get the ticket with ID 16
        Ticket ticket16 = ticketRepository.findAll().stream()
                .filter(t -> t.getTicketId() == 16L)
                .findFirst()
                .orElse(null);

        if (ticket16 == null) {
            // If we can't create ticket with ID 16, create one and manually set the ID for testing
            Ticket testTicket = new Ticket("Test Ticket 16", "Description 16", TicketCategory.HARDWARE, testUser);
            testTicket = ticketRepository.save(testTicket);
            System.out.println("[DEBUG_LOG] Created ticket with actual ID: " + testTicket.getTicketId());

            // Use the actual ticket ID for the test
            Long actualTicketId = testTicket.getTicketId();

            // Create authentication
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    testUser.getMail(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
            );

            // Create comment request
            CreateCommentRequest request = new CreateCommentRequest("Comment for existing ticket");

            // Create comment for existing ticket
            System.out.println("[DEBUG_LOG] Attempting to create comment for existing ticket ID: " + actualTicketId);

            var response = ticketService.createTicketComment(actualTicketId, request, auth);

            assertNotNull(response);
            assertEquals("Comment for existing ticket", response.getComment());
            assertNotNull(response.getAuthorUsername());
            assertNotNull(response.getCommentDate());
            System.out.println("[DEBUG_LOG] Successfully created comment for ticket ID: " + actualTicketId);
        } else {
            System.out.println("[DEBUG_LOG] Found ticket with ID 16: " + ticket16.getTicketId());

            // Create authentication
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    testUser.getMail(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
            );

            // Create comment request
            CreateCommentRequest request = new CreateCommentRequest("Comment for ticket 16");

            // Create comment for ticket 16
            System.out.println("[DEBUG_LOG] Attempting to create comment for ticket ID 16");

            var response = ticketService.createTicketComment(16L, request, auth);

            assertNotNull(response);
            assertEquals("Comment for ticket 16", response.getComment());
            assertNotNull(response.getAuthorUsername());
            assertNotNull(response.getCommentDate());
            System.out.println("[DEBUG_LOG] Successfully created comment for ticket ID 16");
        }
    }

    @Test
    void testRaceConditionScenario() {
        // Create a ticket
        Ticket testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, testUser);
        testTicket = ticketRepository.save(testTicket);
        Long ticketId = testTicket.getTicketId();

        System.out.println("[DEBUG_LOG] Created ticket with ID: " + ticketId);

        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest("Comment before deletion");

        // First, create a comment successfully
        var response1 = ticketService.createTicketComment(ticketId, request, auth);
        assertNotNull(response1);
        System.out.println("[DEBUG_LOG] Successfully created first comment");

        // Now delete the ticket to simulate the race condition
        ticketRepository.delete(testTicket);
        System.out.println("[DEBUG_LOG] Deleted ticket with ID: " + ticketId);

        // Try to create another comment - this should fail gracefully
        CreateCommentRequest request2 = new CreateCommentRequest("Comment after deletion");

        System.out.println("[DEBUG_LOG] Attempting to create comment for deleted ticket ID: " + ticketId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicketComment(ticketId, request2, auth);
        });

        System.out.println("[DEBUG_LOG] Actual exception message: '" + exception.getMessage() + "'");
        // The exception message should indicate the ticket was not found or deleted
        assertTrue(exception.getMessage().contains("Ticket not found") || 
                  exception.getMessage().contains("has been deleted"),
                  "Expected message about ticket not found, but got: " + exception.getMessage());
        System.out.println("[DEBUG_LOG] Correctly handled deleted ticket scenario: " + exception.getMessage());
    }
}
