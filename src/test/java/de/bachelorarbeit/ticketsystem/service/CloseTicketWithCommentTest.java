package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CloseTicketWithCommentTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    private UserAccount endUser;
    private UserAccount supportUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Create test users
        endUser = new UserAccount("enduser", "enduser@test.com", "hashedPassword", Role.ENDUSER);
        supportUser = new UserAccount("supportuser", "support@test.com", "hashedPassword", Role.SUPPORTUSER);

        userRepository.save(endUser);
        userRepository.save(supportUser);

        // Create test ticket
        testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, endUser);
        testTicket = ticketRepository.save(testTicket);

        System.out.println("[DEBUG_LOG] Test setup completed with ticket ID: " + testTicket.getTicketId());
    }

    @Test
    void testCloseTicketWithCommentSuccess() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Close ticket with comment
        String closingComment = "Issue resolved successfully";
        TicketResponse response = ticketService.closeTicketWithComment(testTicket.getTicketId(), closingComment, supportAuth);

        // Verify ticket is closed
        assertEquals(TicketState.CLOSED, response.getTicketState());
        assertNotNull(response.getClosedDate());

        // Verify comment was created
        List<TicketComment> comments = ticketCommentRepository.findByTicket(testTicket);
        assertEquals(1, comments.size());
        assertEquals(closingComment, comments.get(0).getComment());
        assertEquals(supportUser.getMail(), comments.get(0).getCommentUser().getMail());

        System.out.println("[DEBUG_LOG] Successfully closed ticket with comment");
    }

    @Test
    void testCloseTicketWithBlankCommentFails() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Try to close ticket with blank comment
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.closeTicketWithComment(testTicket.getTicketId(), "   ", supportAuth);
        });

        assertTrue(exception.getMessage().contains("Closing a ticket requires a concluding comment"));
        System.out.println("[DEBUG_LOG] Correctly rejected blank comment: " + exception.getMessage());
    }

    @Test
    void testCloseTicketWithNullCommentFails() {
        Authentication supportAuth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORTUSER"))
        );

        // Assign ticket first
        ticketService.assignTicketToCurrentSupport(testTicket.getTicketId(), supportAuth);

        // Try to close ticket with null comment
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.closeTicketWithComment(testTicket.getTicketId(), null, supportAuth);
        });

        assertTrue(exception.getMessage().contains("Closing a ticket requires a concluding comment"));
        System.out.println("[DEBUG_LOG] Correctly rejected null comment: " + exception.getMessage());
    }

    @Test
    void testEndUserCannotCloseTicket() {
        Authentication endUserAuth = new UsernamePasswordAuthenticationToken(
                endUser.getMail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ENDUSER"))
        );

        // Try to close ticket as end user
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.closeTicketWithComment(testTicket.getTicketId(), "Trying to close", endUserAuth);
        });

        assertTrue(exception.getMessage().contains("Only support/admin users can close tickets"));
        System.out.println("[DEBUG_LOG] Correctly rejected end user close attempt: " + exception.getMessage());
    }
}
