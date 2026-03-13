package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.SupportTicketAssignment;
import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.SupportTicketAssignmentRepository;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify support user comment authorization on assigned tickets.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SupportCommentAuthorizationTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SupportTicketAssignmentRepository supportTicketAssignmentRepository;

    private UserAccount endUser;
    private UserAccount supportUser;
    private UserAccount otherSupportUser;
    private UserAccount adminUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Clean up
        supportTicketAssignmentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        endUser = new UserAccount("enduser", "enduser@example.com", "password", Role.ENDUSER);
        endUser = userRepository.save(endUser);

        supportUser = new UserAccount("supportuser", "support@example.com", "password", Role.SUPPORTUSER);
        supportUser = userRepository.save(supportUser);

        otherSupportUser = new UserAccount("othersupport", "othersupport@example.com", "password", Role.SUPPORTUSER);
        otherSupportUser = userRepository.save(otherSupportUser);

        adminUser = new UserAccount("adminuser", "admin@example.com", "password", Role.ADMINUSER);
        adminUser = userRepository.save(adminUser);

        // Create test ticket
        testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, endUser);
        testTicket = ticketRepository.save(testTicket);

        System.out.println("[DEBUG_LOG] Created test ticket with ID: " + testTicket.getTicketId());
    }

    @Test
    void testSupportUserCanCommentOnAssignedTicket() {
        // Assign ticket to support user
        SupportTicketAssignment assignment = new SupportTicketAssignment(testTicket, supportUser);
        supportTicketAssignmentRepository.save(assignment);
        testTicket.setAssignedSupportUser(supportUser);
        ticketRepository.save(testTicket);

        // Create authentication for support user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                null
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("Support comment on assigned ticket");

        // This should succeed
        assertDoesNotThrow(() -> {
            var response = ticketService.createTicketComment(testTicket.getTicketId(), request, auth);
            assertNotNull(response);
            assertEquals("Support comment on assigned ticket", response.getComment());
            System.out.println("[DEBUG_LOG] Support user successfully commented on assigned ticket");
        });
    }

    @Test
    void testSupportUserCannotCommentOnUnassignedTicket() {
        // Create authentication for support user (ticket is not assigned to them)
        Authentication auth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                null
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("Support comment on unassigned ticket");

        // This should fail with the new authorization logic
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            ticketService.createTicketComment(testTicket.getTicketId(), request, auth);
        });

        assertTrue(exception.getMessage().contains("Access denied"));
        System.out.println("[DEBUG_LOG] Support user correctly denied access to unassigned ticket: " + exception.getMessage());
    }

    @Test
    void testSupportUserCannotCommentOnTicketAssignedToOther() {
        // Assign ticket to other support user
        SupportTicketAssignment assignment = new SupportTicketAssignment(testTicket, otherSupportUser);
        supportTicketAssignmentRepository.save(assignment);
        testTicket.setAssignedSupportUser(otherSupportUser);
        ticketRepository.save(testTicket);

        // Create authentication for first support user (ticket is assigned to other support user)
        Authentication auth = new UsernamePasswordAuthenticationToken(
                supportUser.getMail(),
                null,
                null
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("Support comment on other's ticket");

        // This should fail with the new authorization logic
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            ticketService.createTicketComment(testTicket.getTicketId(), request, auth);
        });

        assertTrue(exception.getMessage().contains("Access denied"));
        System.out.println("[DEBUG_LOG] Support user correctly denied access to ticket assigned to other: " + exception.getMessage());
    }

    @Test
    void testAdminUserCanCommentOnAnyTicket() {
        // Assign ticket to support user (admin should still be able to comment)
        SupportTicketAssignment assignment = new SupportTicketAssignment(testTicket, supportUser);
        supportTicketAssignmentRepository.save(assignment);
        testTicket.setAssignedSupportUser(supportUser);
        ticketRepository.save(testTicket);

        // Create authentication for admin user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                adminUser.getMail(),
                null,
                null
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("Admin comment on any ticket");

        // This should succeed - admin can comment on any ticket
        assertDoesNotThrow(() -> {
            var response = ticketService.createTicketComment(testTicket.getTicketId(), request, auth);
            assertNotNull(response);
            assertEquals("Admin comment on any ticket", response.getComment());
            System.out.println("[DEBUG_LOG] Admin user successfully commented on any ticket");
        });
    }

    @Test
    void testEndUserCanStillCommentOnOwnTicket() {
        // Create authentication for end user (ticket owner)
        Authentication auth = new UsernamePasswordAuthenticationToken(
                endUser.getMail(),
                null,
                null
        );

        // Create comment request
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("End user comment on own ticket");

        // This should still work
        assertDoesNotThrow(() -> {
            var response = ticketService.createTicketComment(testTicket.getTicketId(), request, auth);
            assertNotNull(response);
            assertEquals("End user comment on own ticket", response.getComment());
            System.out.println("[DEBUG_LOG] End user successfully commented on own ticket");
        });
    }
}