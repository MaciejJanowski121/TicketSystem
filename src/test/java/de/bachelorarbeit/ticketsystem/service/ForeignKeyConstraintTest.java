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
 * Test to reproduce and fix foreign key constraint violation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ForeignKeyConstraintTest {

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
    void testCreateCommentForNonExistentTicket() {
        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest("This should fail");

        // Try to create comment for non-existent ticket ID 15
        System.out.println("[DEBUG_LOG] Attempting to create comment for non-existent ticket ID 15");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicketComment(15L, request, auth);
        });

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Correctly threw IllegalArgumentException: " + exception.getMessage());
    }

    @Test
    void testCreateCommentForExistingTicket() {
        // Create a ticket first
        Ticket testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, testUser);
        testTicket = ticketRepository.save(testTicket);

        System.out.println("[DEBUG_LOG] Created ticket with ID: " + testTicket.getTicketId());

        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest("This should work");

        // Create comment for existing ticket
        System.out.println("[DEBUG_LOG] Attempting to create comment for existing ticket ID: " + testTicket.getTicketId());

        var response = ticketService.createTicketComment(testTicket.getTicketId(), request, auth);

        assertNotNull(response);
        assertEquals("This should work", response.getComment());
        assertEquals(testTicket.getTicketId(), response.getTicketId());
        System.out.println("[DEBUG_LOG] Successfully created comment for ticket ID: " + response.getTicketId());
    }

    @Test
    void testDirectTicketCommentCreation() {
        // Create a ticket first
        Ticket testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, testUser);
        testTicket = ticketRepository.save(testTicket);

        System.out.println("[DEBUG_LOG] Created ticket with ID: " + testTicket.getTicketId());

        // Try to create TicketComment directly
        TicketComment comment = new TicketComment(testTicket, testUser, "Direct creation test");

        System.out.println("[DEBUG_LOG] TicketComment created with ticket ID in composite key: " + comment.getTc_pk().getTicketId());

        // Save the comment
        TicketComment savedComment = ticketCommentRepository.save(comment);

        assertNotNull(savedComment);
        assertEquals(testTicket.getTicketId(), savedComment.getTc_pk().getTicketId());
        System.out.println("[DEBUG_LOG] Successfully saved comment directly with ticket ID: " + savedComment.getTc_pk().getTicketId());
    }

    @Test
    void testForeignKeyConstraintViolationHandling() {
        // Create a ticket first
        Ticket testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, testUser);
        testTicket = ticketRepository.save(testTicket);
        Long ticketId = testTicket.getTicketId();

        System.out.println("[DEBUG_LOG] Created ticket with ID: " + ticketId);

        // Delete the ticket to simulate the constraint violation scenario
        ticketRepository.delete(testTicket);
        System.out.println("[DEBUG_LOG] Deleted ticket with ID: " + ticketId);

        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest("This should fail with constraint violation");

        // Try to create comment for deleted ticket - should be handled gracefully
        System.out.println("[DEBUG_LOG] Attempting to create comment for deleted ticket ID: " + ticketId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicketComment(ticketId, request, auth);
        });

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Correctly handled deleted ticket scenario: " + exception.getMessage());
    }
}
