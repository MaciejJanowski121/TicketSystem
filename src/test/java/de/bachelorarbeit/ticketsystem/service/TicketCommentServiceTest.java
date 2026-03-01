package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.dto.CommentResponse;
import de.bachelorarbeit.ticketsystem.model.entity.*;
import de.bachelorarbeit.ticketsystem.repository.TicketCommentRepository;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ticket comment functionality.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketCommentServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @BeforeEach
    void setUp() {
        ticketCommentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateCommentRequestValidation() {
        // Test CreateCommentRequest validation
        CreateCommentRequest request = new CreateCommentRequest();
        
        // Test with null value
        assertNull(request.getComment());

        // Test with valid value
        request.setComment("This is a test comment");
        assertEquals("This is a test comment", request.getComment());

        // Test constructor
        CreateCommentRequest request2 = new CreateCommentRequest("Another comment");
        assertEquals("Another comment", request2.getComment());

        System.out.println("[DEBUG_LOG] CreateCommentRequest validation test completed");
    }

    @Test
    void testCommentResponseCreation() {
        // Test CommentResponse creation
        CommentResponse response = new CommentResponse();
        
        // Test setters
        response.setTicketId(1L);
        response.setCommentUserMail("test@example.com");
        response.setCommentUserName("testuser");
        response.setCommentDate(java.time.Instant.now());
        response.setComment("Test comment");

        // Verify getters
        assertEquals(1L, response.getTicketId());
        assertEquals("test@example.com", response.getCommentUserMail());
        assertEquals("testuser", response.getCommentUserName());
        assertEquals("Test comment", response.getComment());
        assertNotNull(response.getCommentDate());

        System.out.println("[DEBUG_LOG] CommentResponse creation test completed");
    }

    @Test
    void testTicketCommentEntityCreation() {
        // Create test user
        UserAccount testUser = new UserAccount("testuser", "test@example.com", "hashedpassword", Role.ENDUSER);
        testUser = userRepository.save(testUser);

        // Create test ticket
        Ticket testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, testUser);
        testTicket = ticketRepository.save(testTicket);

        // Create comment using entity constructor
        TicketComment comment = new TicketComment(testTicket, testUser, "This is a test comment");
        
        // Verify comment properties
        assertEquals(testTicket, comment.getTicket());
        assertEquals(testUser, comment.getCommentUser());
        assertEquals("This is a test comment", comment.getComment());
        assertNotNull(comment.getTc_pk());
        assertNotNull(comment.getCommentDate());

        // Save comment
        TicketComment savedComment = ticketCommentRepository.save(comment);
        assertNotNull(savedComment);

        System.out.println("[DEBUG_LOG] TicketComment entity creation test completed");
    }

    @Test
    void testTicketCommentRepositoryFunctionality() {
        // Create test user
        UserAccount testUser = new UserAccount("testuser", "test@example.com", "hashedpassword", Role.ENDUSER);
        testUser = userRepository.save(testUser);

        // Create test ticket
        Ticket testTicket = new Ticket("Test Ticket", "Test Description", TicketCategory.HARDWARE, testUser);
        testTicket = ticketRepository.save(testTicket);

        // Verify repository is initially empty
        assertEquals(0, ticketCommentRepository.count());

        // Create and save comment
        TicketComment comment = new TicketComment(testTicket, testUser, "Repository test comment");
        ticketCommentRepository.save(comment);

        // Verify comment was saved
        assertEquals(1, ticketCommentRepository.count());

        // Test findByTicket method
        var comments = ticketCommentRepository.findByTicket(testTicket);
        assertEquals(1, comments.size());
        assertEquals("Repository test comment", comments.get(0).getComment());

        System.out.println("[DEBUG_LOG] TicketCommentRepository functionality test completed");
    }

    @Test
    void testUserRolesForComments() {
        // Test different user roles
        UserAccount endUser = new UserAccount("enduser", "enduser@example.com", "password", Role.ENDUSER);
        UserAccount supportUser = new UserAccount("supportuser", "support@example.com", "password", Role.SUPPORTUSER);
        UserAccount adminUser = new UserAccount("adminuser", "admin@example.com", "password", Role.ADMINUSER);

        endUser = userRepository.save(endUser);
        supportUser = userRepository.save(supportUser);
        adminUser = userRepository.save(adminUser);

        // Verify roles
        assertEquals(Role.ENDUSER, endUser.getRole());
        assertEquals(Role.SUPPORTUSER, supportUser.getRole());
        assertEquals(Role.ADMINUSER, adminUser.getRole());

        System.out.println("[DEBUG_LOG] User roles for comments test completed");
    }
}