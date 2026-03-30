package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for updateDate functionality in tickets.
 * Verifies that updateDate is properly set and automatically updated.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketUpdateDateTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    void testUpdateDateSetOnTicketCreation() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");

        // Create authentication object
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Record time before ticket creation
        Instant beforeCreation = Instant.now();

        // Create ticket request
        CreateTicketRequest request = new CreateTicketRequest(
                "Test Ticket",
                "Test description",
                TicketCategory.HARDWARE
        );

        // Create ticket
        TicketResponse response = ticketService.createTicket(request, auth);

        // Record time after ticket creation
        Instant afterCreation = Instant.now();

        // Verify updateDate is set and within expected range
        assertNotNull(response.getUpdateDate());
        assertTrue(response.getUpdateDate().isAfter(beforeCreation.minusSeconds(1))); // Allow 1 second buffer
        assertTrue(response.getUpdateDate().isBefore(afterCreation.plusSeconds(1))); // Allow 1 second buffer

        // Verify updateDate equals createDate initially
        assertEquals(response.getCreateDate(), response.getUpdateDate());

        System.out.println("[DEBUG_LOG] UpdateDate set correctly on ticket creation: " + response.getUpdateDate());
    }

    @Test
    void testUpdateDateChangesWhenCommentAdded() throws InterruptedException {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");

        // Create authentication object
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create ticket
        CreateTicketRequest request = new CreateTicketRequest(
                "Original Title",
                "Original description",
                TicketCategory.HARDWARE
        );

        TicketResponse createdTicket = ticketService.createTicket(request, auth);
        Instant originalUpdateDate = createdTicket.getUpdateDate();

        System.out.println("[DEBUG_LOG] Original updateDate: " + originalUpdateDate);

        // Wait a small amount to ensure time difference
        Thread.sleep(100);

        // Record time before modification
        Instant beforeModification = Instant.now();

        // Add a comment to the ticket (this should update the updateDate)
        CreateCommentRequest commentRequest = new CreateCommentRequest();
        commentRequest.setComment("This is a test comment");

        ticketService.createTicketComment(createdTicket.getTicketId(), commentRequest, auth);

        // Record time after modification
        Instant afterModification = Instant.now();

        // Get the updated ticket
        TicketResponse updatedTicket = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);

        // Verify updateDate was changed
        assertNotNull(updatedTicket.getUpdateDate());
        assertNotEquals(originalUpdateDate, updatedTicket.getUpdateDate());
        assertTrue(updatedTicket.getUpdateDate().isAfter(originalUpdateDate));
        assertTrue(updatedTicket.getUpdateDate().isAfter(beforeModification.minusSeconds(1)));
        assertTrue(updatedTicket.getUpdateDate().isBefore(afterModification.plusSeconds(1)));

        // Verify createDate remains unchanged
        assertEquals(createdTicket.getCreateDate(), updatedTicket.getCreateDate());

        System.out.println("[DEBUG_LOG] UpdateDate changed after adding comment: " + updatedTicket.getUpdateDate());
        System.out.println("[DEBUG_LOG] Time difference: " + 
            (updatedTicket.getUpdateDate().toEpochMilli() - originalUpdateDate.toEpochMilli()) + "ms");
    }

    @Test
    void testUpdateDateChangesOnMultipleComments() throws InterruptedException {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");

        // Create authentication object
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create ticket
        CreateTicketRequest request = new CreateTicketRequest(
                "Multiple Comments Test",
                "Testing multiple comments updateDate",
                TicketCategory.NETWORK
        );

        TicketResponse createdTicket = ticketService.createTicket(request, auth);
        Instant originalUpdateDate = createdTicket.getUpdateDate();

        System.out.println("[DEBUG_LOG] Original updateDate: " + originalUpdateDate);

        // Wait to ensure time difference
        Thread.sleep(100);

        // Add first comment
        CreateCommentRequest commentRequest1 = new CreateCommentRequest();
        commentRequest1.setComment("First comment");
        ticketService.createTicketComment(createdTicket.getTicketId(), commentRequest1, auth);

        TicketResponse afterFirstComment = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
        Instant firstCommentUpdateDate = afterFirstComment.getUpdateDate();

        System.out.println("[DEBUG_LOG] UpdateDate after first comment: " + firstCommentUpdateDate);

        // Wait and add second comment
        Thread.sleep(100);
        CreateCommentRequest commentRequest2 = new CreateCommentRequest();
        commentRequest2.setComment("Second comment");
        ticketService.createTicketComment(createdTicket.getTicketId(), commentRequest2, auth);

        TicketResponse afterSecondComment = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
        Instant secondCommentUpdateDate = afterSecondComment.getUpdateDate();

        // Verify updateDate was updated with each comment
        assertTrue(firstCommentUpdateDate.isAfter(originalUpdateDate));
        assertTrue(secondCommentUpdateDate.isAfter(firstCommentUpdateDate));

        System.out.println("[DEBUG_LOG] UpdateDate after second comment: " + secondCommentUpdateDate);
    }

    @Test
    void testUpdateDateConsistencyAcrossOperations() throws InterruptedException {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");

        // Create authentication object
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create ticket
        CreateTicketRequest request = new CreateTicketRequest(
                "Consistency Test",
                "Testing updateDate consistency",
                TicketCategory.HARDWARE
        );

        TicketResponse createdTicket = ticketService.createTicket(request, auth);
        Instant originalUpdateDate = createdTicket.getUpdateDate();

        System.out.println("[DEBUG_LOG] Original updateDate: " + originalUpdateDate);

        // Wait to ensure time difference
        Thread.sleep(100);

        // Add a comment (this should update updateDate)
        CreateCommentRequest commentRequest = new CreateCommentRequest();
        commentRequest.setComment("Testing consistency");
        ticketService.createTicketComment(createdTicket.getTicketId(), commentRequest, auth);

        // Get the ticket after comment
        TicketResponse afterComment = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
        Instant afterCommentUpdateDate = afterComment.getUpdateDate();

        // Verify updateDate was updated and is consistent
        assertNotNull(afterCommentUpdateDate);
        assertTrue(afterCommentUpdateDate.isAfter(originalUpdateDate));
        assertEquals(createdTicket.getCreateDate(), afterComment.getCreateDate()); // createDate should remain unchanged

        // Verify that the updateDate is properly set and not null
        assertNotNull(afterComment.getUpdateDate());
        assertTrue(afterComment.getUpdateDate().equals(afterCommentUpdateDate));

        System.out.println("[DEBUG_LOG] UpdateDate consistency verified: " + afterCommentUpdateDate);
    }

    @Test
    void testUpdateDateWithSequentialComments() throws InterruptedException {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");

        // Create authentication object
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create ticket
        CreateTicketRequest request = new CreateTicketRequest(
                "Sequential Comments Test",
                "Testing sequential comments",
                TicketCategory.OTHER
        );

        TicketResponse createdTicket = ticketService.createTicket(request, auth);
        Instant originalUpdateDate = createdTicket.getUpdateDate();

        System.out.println("[DEBUG_LOG] Original updateDate: " + originalUpdateDate);

        // First comment
        Thread.sleep(50);
        CreateCommentRequest comment1 = new CreateCommentRequest();
        comment1.setComment("First comment");
        ticketService.createTicketComment(createdTicket.getTicketId(), comment1, auth);

        TicketResponse afterFirst = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
        Instant firstCommentDate = afterFirst.getUpdateDate();

        System.out.println("[DEBUG_LOG] After first comment: " + firstCommentDate);

        // Second comment
        Thread.sleep(50);
        CreateCommentRequest comment2 = new CreateCommentRequest();
        comment2.setComment("Second comment");
        ticketService.createTicketComment(createdTicket.getTicketId(), comment2, auth);

        TicketResponse afterSecond = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
        Instant secondCommentDate = afterSecond.getUpdateDate();

        System.out.println("[DEBUG_LOG] After second comment: " + secondCommentDate);

        // Third comment
        Thread.sleep(50);
        CreateCommentRequest comment3 = new CreateCommentRequest();
        comment3.setComment("Third comment");
        ticketService.createTicketComment(createdTicket.getTicketId(), comment3, auth);

        TicketResponse afterThird = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
        Instant thirdCommentDate = afterThird.getUpdateDate();

        System.out.println("[DEBUG_LOG] After third comment: " + thirdCommentDate);

        // Verify each comment updated the updateDate
        assertTrue(firstCommentDate.isAfter(originalUpdateDate));
        assertTrue(secondCommentDate.isAfter(firstCommentDate));
        assertTrue(thirdCommentDate.isAfter(secondCommentDate));

        // Verify createDate remains unchanged
        assertEquals(createdTicket.getCreateDate(), afterThird.getCreateDate());

        System.out.println("[DEBUG_LOG] Sequential comments test completed successfully");
    }

    @Test
    void testUpdateDateNotChangedOnRead() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");

        // Create authentication object
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create ticket
        CreateTicketRequest request = new CreateTicketRequest(
                "Read Test",
                "Testing that reads don't change updateDate",
                TicketCategory.PROGRAMS_TOOLS
        );

        TicketResponse createdTicket = ticketService.createTicket(request, auth);
        Instant originalUpdateDate = createdTicket.getUpdateDate();

        System.out.println("[DEBUG_LOG] Original updateDate before reads: " + originalUpdateDate);

        // Read the ticket multiple times
        for (int i = 0; i < 5; i++) {
            TicketResponse readTicket = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);
            assertEquals(originalUpdateDate, readTicket.getUpdateDate());
        }

        // Verify updateDate hasn't changed after multiple reads
        Ticket ticket = ticketRepository.findById(createdTicket.getTicketId()).orElseThrow();
        assertEquals(originalUpdateDate, ticket.getUpdateDate());

        System.out.println("[DEBUG_LOG] UpdateDate unchanged after multiple reads: " + ticket.getUpdateDate());
    }
}
