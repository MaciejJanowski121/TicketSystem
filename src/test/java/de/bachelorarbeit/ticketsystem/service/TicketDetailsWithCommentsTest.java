package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketCommentResponse;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that the GET /api/tickets/{ticketId} endpoint includes comments in the response.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketDetailsWithCommentsTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    private UserAccount testUser;
    private UserAccount supportUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        ticketCommentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser = new UserAccount("testuser", "test@example.com", "hashedpassword", Role.ENDUSER);
        testUser = userRepository.save(testUser);

        supportUser = new UserAccount("supportuser", "support@example.com", "hashedpassword", Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);

        // Create a test ticket
        testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, testUser);
        testTicket = ticketRepository.save(testTicket);

        System.out.println("[DEBUG_LOG] Created ticket with ID: " + testTicket.getTicketId());
    }

    @Test
    void testGetTicketByIdWithNoComments() {
        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Get ticket details
        TicketResponse response = ticketService.getTicketById(testTicket.getTicketId(), auth);

        // Verify ticket details
        assertNotNull(response);
        assertEquals(testTicket.getTicketId(), response.getTicketId());
        assertEquals("Test Ticket", response.getTitle());
        assertEquals("Test Description", response.getDescription());

        // Verify comments field exists and is empty list (not null)
        assertNotNull(response.getComments());
        assertTrue(response.getComments().isEmpty());

        System.out.println("[DEBUG_LOG] Ticket details response includes empty comments list");
    }

    @Test
    void testGetTicketByIdWithComments() {
        // Add comments to the ticket
        TicketComment comment1 = new TicketComment(testTicket, testUser, "First comment by user");
        ticketCommentRepository.save(comment1);

        TicketComment comment2 = new TicketComment(testTicket, supportUser, "Response from support");
        ticketCommentRepository.save(comment2);

        System.out.println("[DEBUG_LOG] Added 2 comments to ticket");

        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Get ticket details
        TicketResponse response = ticketService.getTicketById(testTicket.getTicketId(), auth);

        // Verify ticket details
        assertNotNull(response);
        assertEquals(testTicket.getTicketId(), response.getTicketId());

        // Verify comments are included
        assertNotNull(response.getComments());
        assertEquals(2, response.getComments().size());

        List<TicketCommentResponse> comments = response.getComments();

        // Verify first comment
        TicketCommentResponse firstComment = comments.get(0);
        assertEquals("First comment by user", firstComment.getComment());
        assertEquals(testUser.getUsername(), firstComment.getAuthorUsername());
        assertNotNull(firstComment.getCommentDate());

        // Verify second comment
        TicketCommentResponse secondComment = comments.get(1);
        assertEquals("Response from support", secondComment.getComment());
        assertEquals(supportUser.getUsername(), secondComment.getAuthorUsername());
        assertNotNull(secondComment.getCommentDate());

        System.out.println("[DEBUG_LOG] Ticket details response includes " + comments.size() + " comments");
        System.out.println("[DEBUG_LOG] Comment 1: " + firstComment.getComment() + " by " + firstComment.getAuthorUsername());
        System.out.println("[DEBUG_LOG] Comment 2: " + secondComment.getComment() + " by " + secondComment.getAuthorUsername());
    }

    @Test
    void testGetTicketByIdWithCommentsUsesEmailWhenUsernameEmpty() {
        // Create a user with empty username to test fallback to email
        UserAccount userWithoutUsername = new UserAccount("", "nousername@example.com", "password", Role.ENDUSER);
        userWithoutUsername = userRepository.save(userWithoutUsername);

        // Add comment from user without username
        TicketComment comment = new TicketComment(testTicket, userWithoutUsername, "Comment from user without username");
        ticketCommentRepository.save(comment);

        // Create authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Get ticket details
        TicketResponse response = ticketService.getTicketById(testTicket.getTicketId(), auth);

        // Verify comments are included
        assertNotNull(response.getComments());
        assertEquals(1, response.getComments().size());

        TicketCommentResponse commentResponse = response.getComments().get(0);
        assertEquals("Comment from user without username", commentResponse.getComment());
        // Should use email as fallback when username is empty
        assertEquals("nousername@example.com", commentResponse.getAuthorUsername());

        System.out.println("[DEBUG_LOG] Comment author fallback to email: " + commentResponse.getAuthorUsername());
    }
}