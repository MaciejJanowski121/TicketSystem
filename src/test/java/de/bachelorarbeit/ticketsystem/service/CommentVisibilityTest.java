package de.bachelorarbeit.ticketsystem.service;

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
 * Test to verify that comment visibility works correctly for all authenticated users.
 * This test addresses the issue where ENDUSERs couldn't see comments on tickets they didn't create.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommentVisibilityTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    private UserAccount ticketCreator;
    private UserAccount otherEndUser;
    private UserAccount supportUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        ticketCommentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        ticketCreator = new UserAccount("creator", "creator@example.com", "hashedpassword", Role.ENDUSER);
        ticketCreator = userRepository.save(ticketCreator);

        otherEndUser = new UserAccount("otheruser", "other@example.com", "hashedpassword", Role.ENDUSER);
        otherEndUser = userRepository.save(otherEndUser);

        supportUser = new UserAccount("supportuser", "support@example.com", "hashedpassword", Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);

        // Create a ticket by the ticket creator
        testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, ticketCreator);
        testTicket = ticketRepository.save(testTicket);

        // Add some comments to the ticket
        TicketComment comment1 = new TicketComment(testTicket, ticketCreator, "Comment by ticket creator");
        ticketCommentRepository.save(comment1);

        TicketComment comment2 = new TicketComment(testTicket, supportUser, "Comment by support user");
        ticketCommentRepository.save(comment2);

        System.out.println("[DEBUG_LOG] Created ticket with ID: " + testTicket.getTicketId() + " and 2 comments");
    }

    @Test
    void testTicketCreatorCanSeeComments() {
        // Create authentication for ticket creator
        Authentication auth = new UsernamePasswordAuthenticationToken(
                ticketCreator.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Ticket creator should be able to see comments
        List<TicketCommentResponse> comments = ticketService.getTicketComments(testTicket.getTicketId(), auth);

        assertEquals(2, comments.size());
        System.out.println("[DEBUG_LOG] Ticket creator can see " + comments.size() + " comments");
    }

    @Test
    void testOtherEndUserCanSeeComments() {
        // Create authentication for other end user (not the ticket creator)
        Authentication auth = new UsernamePasswordAuthenticationToken(
                otherEndUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Other end user should now be able to see comments (this was the bug)
        List<TicketCommentResponse> comments = ticketService.getTicketComments(testTicket.getTicketId(), auth);

        assertEquals(2, comments.size());
        System.out.println("[DEBUG_LOG] Other end user can see " + comments.size() + " comments");

        // Verify the comments contain the expected content
        boolean hasCreatorComment = comments.stream()
                .anyMatch(c -> c.getComment().equals("Comment by ticket creator"));
        boolean hasSupportComment = comments.stream()
                .anyMatch(c -> c.getComment().equals("Comment by support user"));

        assertTrue(hasCreatorComment, "Should see comment by ticket creator");
        assertTrue(hasSupportComment, "Should see comment by support user");
    }

    @Test
    void testSupportUserCanSeeComments() {
        // Create authentication for support user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Support user should be able to see comments
        List<TicketCommentResponse> comments = ticketService.getTicketComments(testTicket.getTicketId(), auth);

        assertEquals(2, comments.size());
        System.out.println("[DEBUG_LOG] Support user can see " + comments.size() + " comments");
    }

    @Test
    void testCommentCreationStillRestricted() {
        // Create authentication for other end user (not the ticket creator)
        Authentication auth = new UsernamePasswordAuthenticationToken(
                otherEndUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Other end user should NOT be able to create comments on tickets they don't own
        // This restriction should still be in place
        de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest request = 
                new de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest("This should fail");

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            ticketService.createTicketComment(testTicket.getTicketId(), request, auth);
        });

        assertTrue(exception.getMessage().contains("Access denied"));
        System.out.println("[DEBUG_LOG] Comment creation is still properly restricted: " + exception.getMessage());
    }
}
