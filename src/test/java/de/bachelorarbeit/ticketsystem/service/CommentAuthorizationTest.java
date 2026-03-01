package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify comment authorization and error message format.
 */
@SpringBootTest
@ActiveProfiles("test")
public class CommentAuthorizationTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private UserAccount ticketOwner;
    private UserAccount otherEndUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Create ticket owner
        ticketOwner = new UserAccount("owner", "owner@example.com", "hashedpassword", Role.ENDUSER);
        ticketOwner = userRepository.save(ticketOwner);

        // Create another end user
        otherEndUser = new UserAccount("other", "other@example.com", "hashedpassword", Role.ENDUSER);
        otherEndUser = userRepository.save(otherEndUser);

        // Create a ticket owned by ticketOwner
        testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, ticketOwner);
        testTicket = ticketRepository.save(testTicket);
    }

    @Test
    void testEndUserCannotCommentOnOtherUsersTicket() {
        // Create authentication for the other end user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                otherEndUser.getMail(),
                null,
                null
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("This should fail");

        // Attempt to create comment should throw SecurityException with specific message
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            ticketService.createTicketComment(testTicket.getTicketId(), request, auth);
        });

        // Verify the exact error message
        assertEquals("Access denied: You cannot comment on this ticket.", exception.getMessage());
        
        System.out.println("[DEBUG_LOG] Verified error message: " + exception.getMessage());
    }

    @Test
    void testTicketOwnerCanCommentOnOwnTicket() {
        // Create authentication for the ticket owner
        Authentication auth = new UsernamePasswordAuthenticationToken(
                ticketOwner.getMail(),
                null,
                null
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("This should succeed");

        // This should not throw an exception
        assertDoesNotThrow(() -> {
            var response = ticketService.createTicketComment(testTicket.getTicketId(), request, auth);
            assertNotNull(response);
            assertEquals("This should succeed", response.getComment());
            System.out.println("[DEBUG_LOG] Ticket owner successfully commented");
        });
    }
}