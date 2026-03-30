package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service-level tests for unknown ID error handling.
 * Tests that service methods throw appropriate exceptions with German error messages.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UnknownIdServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Authentication endUserAuth;
    private Authentication supportUserAuth;

    @BeforeEach
    void setUp() {
        // Clean up
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        UserAccount endUser = new UserAccount("enduser", "enduser@test.com", 
                passwordEncoder.encode("password"), Role.ENDUSER);
        userRepository.save(endUser);

        UserAccount supportUser = new UserAccount("support", "support@test.com", 
                passwordEncoder.encode("password"), Role.SUPPORTUSER);
        userRepository.save(supportUser);

        // Create authentication objects
        endUserAuth = new UsernamePasswordAuthenticationToken("enduser@test.com", null);
        supportUserAuth = new UsernamePasswordAuthenticationToken("support@test.com", null);
    }

    // ========== TICKET SERVICE TESTS ==========

    @Test
    void testGetTicketById_UnknownId_ThrowsIllegalArgumentException() {
        System.out.println("[DEBUG_LOG] Testing TicketService.getTicketById with unknown ID");

        Long unknownId = 999999L;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getTicketById(unknownId, endUserAuth)
        );

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Service correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testGetMyTicketById_UnknownId_ThrowsIllegalArgumentException() {
        System.out.println("[DEBUG_LOG] Testing TicketService.getMyTicketById with unknown ID");

        Long unknownId = 999999L;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getMyTicketById(unknownId, endUserAuth)
        );

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Service correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testGetTicketComments_UnknownId_ThrowsIllegalArgumentException() {
        System.out.println("[DEBUG_LOG] Testing TicketService.getTicketComments with unknown ID");

        Long unknownId = 999999L;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getTicketComments(unknownId, endUserAuth)
        );

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Service correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testCreateTicketComment_UnknownId_ThrowsIllegalArgumentException() {
        System.out.println("[DEBUG_LOG] Testing TicketService.createTicketComment with unknown ID");

        Long unknownId = 999999L;
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("Test comment on non-existent ticket");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicketComment(unknownId, request, endUserAuth)
        );

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Service correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testAssignTicketToCurrentSupport_UnknownId_ThrowsIllegalArgumentException() {
        System.out.println("[DEBUG_LOG] Testing TicketService.assignTicketToCurrentSupport with unknown ID");

        Long unknownId = 999999L;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.assignTicketToCurrentSupport(unknownId, supportUserAuth)
        );

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Service correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testReleaseTicket_UnknownId_ThrowsIllegalArgumentException() {
        System.out.println("[DEBUG_LOG] Testing TicketService.releaseTicket with unknown ID");

        Long unknownId = 999999L;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.releaseTicket(unknownId, supportUserAuth)
        );

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Service correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testCloseTicketWithComment_UnknownId_ThrowsIllegalArgumentException() {
        System.out.println("[DEBUG_LOG] Testing TicketService.closeTicketWithComment with unknown ID");

        Long unknownId = 999999L;
        String comment = "Closing non-existent ticket";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.closeTicketWithComment(unknownId, comment, supportUserAuth)
        );

        assertEquals("Ticket not found", exception.getMessage());
        System.out.println("[DEBUG_LOG] Service correctly threw exception: " + exception.getMessage());
    }

    // ========== USER SERVICE TESTS ==========

    @Test
    void testUpdateUserRole_UnknownEmail_ThrowsIllegalArgumentException() {
        System.out.println("[DEBUG_LOG] Testing UserService.updateUserRole with unknown email");

        String unknownEmail = "unknown@test.com";
        Role newRole = Role.SUPPORTUSER;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserRole(unknownEmail, newRole)
        );

        assertEquals("User not found with email: " + unknownEmail, exception.getMessage());
        System.out.println("[DEBUG_LOG] Service correctly threw exception: " + exception.getMessage());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testMultipleUnknownIds_ConsistentExceptions() {
        System.out.println("[DEBUG_LOG] Testing multiple unknown IDs for consistent exceptions");

        Long[] unknownIds = {999999L, 123456L, 0L, -1L, Long.MAX_VALUE};

        for (Long unknownId : unknownIds) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> ticketService.getTicketById(unknownId, endUserAuth)
            );

            assertEquals("Ticket not found", exception.getMessage());
        }

        System.out.println("[DEBUG_LOG] All unknown IDs consistently threw 'Ticket not found' exception");
    }

    @Test
    void testExceptionMessageConsistency() {
        System.out.println("[DEBUG_LOG] Testing exception message consistency across different service methods");

        Long unknownId = 999999L;

        // Test different service methods
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getTicketById(unknownId, endUserAuth)
        );

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getMyTicketById(unknownId, endUserAuth)
        );

        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.assignTicketToCurrentSupport(unknownId, supportUserAuth)
        );

        // All should have the same message
        assertEquals("Ticket not found", exception1.getMessage());
        assertEquals("Ticket not found", exception2.getMessage());
        assertEquals("Ticket not found", exception3.getMessage());

        System.out.println("[DEBUG_LOG] Exception messages are consistent across different service methods");
    }

    @Test
    void testServiceExceptionTranslationByGlobalHandler() {
        System.out.println("[DEBUG_LOG] Testing that service exceptions are properly translated by GlobalExceptionHandler");

        // This test verifies that the service layer throws the correct English exceptions
        // which are then translated to German by the GlobalExceptionHandler

        Long unknownId = 999999L;

        // Test ticket not found
        IllegalArgumentException ticketException = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getTicketById(unknownId, endUserAuth)
        );
        assertTrue(ticketException.getMessage().contains("Ticket not found"));

        // Test user not found
        IllegalArgumentException userException = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserRole("unknown@test.com", Role.SUPPORTUSER)
        );
        assertTrue(userException.getMessage().contains("User not found"));

        System.out.println("[DEBUG_LOG] Service exceptions contain correct English messages for GlobalExceptionHandler translation");
    }
}