package de.bachelorarbeit.ticketsystem;

import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import de.bachelorarbeit.ticketsystem.service.AuthService;
import de.bachelorarbeit.ticketsystem.service.TicketService;
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
 * Test to analyze Hibernate queries during ticket creation.
 * This test reproduces the scenario from the issue description to understand
 * why there are multiple user account queries.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class HibernateQueryAnalysisTest {

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
    void testTicketCreationHibernateQueries() {
        System.out.println("[DEBUG_LOG] Starting ticket creation test to analyze Hibernate queries");
        
        // Register a user
        String token = authService.register("testuser", "test@example.com", "password123");
        System.out.println("[DEBUG_LOG] User registered successfully");

        // Create authentication object with username (this should trigger first user query)
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);
        System.out.println("[DEBUG_LOG] Authentication created with username: testuser");

        // Create ticket request
        CreateTicketRequest request = new CreateTicketRequest(
                "Hardware Issue Analysis",
                "Testing Hibernate query behavior during ticket creation",
                TicketCategory.HARDWARE
        );
        System.out.println("[DEBUG_LOG] CreateTicketRequest prepared");

        // Create ticket - this should show the Hibernate queries mentioned in the issue
        System.out.println("[DEBUG_LOG] About to call ticketService.createTicket() - watch for Hibernate queries:");
        TicketResponse response = ticketService.createTicket(request, auth);
        System.out.println("[DEBUG_LOG] Ticket created successfully with ID: " + response.getTicketId());

        // Verify the ticket was created correctly
        assertNotNull(response);
        assertNotNull(response.getTicketId());
        assertEquals("Hardware Issue Analysis", response.getTitle());
        assertEquals("Testing Hibernate query behavior during ticket creation", response.getDescription());
        assertEquals(TicketCategory.HARDWARE, response.getTicketCategory());

        System.out.println("[DEBUG_LOG] Test completed - check the logs above for Hibernate queries");
    }

    @Test
    void testTicketCreationWithEmailAuthentication() {
        System.out.println("[DEBUG_LOG] Starting ticket creation test with email authentication");
        
        // Register a user
        String token = authService.register("emailuser", "emailuser@example.com", "password123");
        System.out.println("[DEBUG_LOG] User registered successfully");

        // Create authentication object with email (this should trigger first user query)
        Authentication auth = new UsernamePasswordAuthenticationToken("emailuser@example.com", null);
        System.out.println("[DEBUG_LOG] Authentication created with email: emailuser@example.com");

        // Create ticket request
        CreateTicketRequest request = new CreateTicketRequest(
                "Email Auth Test",
                "Testing with email authentication",
                TicketCategory.NETWORK
        );

        // Create ticket
        System.out.println("[DEBUG_LOG] About to call ticketService.createTicket() with email auth:");
        TicketResponse response = ticketService.createTicket(request, auth);
        System.out.println("[DEBUG_LOG] Ticket created successfully with ID: " + response.getTicketId());

        // Verify the ticket was created correctly
        assertNotNull(response);
        assertEquals("Email Auth Test", response.getTitle());
    }

    @Test
    void testMultipleTicketCreations() {
        System.out.println("[DEBUG_LOG] Testing multiple ticket creations to see query patterns");
        
        // Register a user
        authService.register("multiuser", "multi@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("multiuser", null);

        // Create first ticket
        System.out.println("[DEBUG_LOG] Creating first ticket:");
        CreateTicketRequest request1 = new CreateTicketRequest(
                "First Ticket",
                "First ticket description",
                TicketCategory.HARDWARE
        );
        TicketResponse response1 = ticketService.createTicket(request1, auth);
        System.out.println("[DEBUG_LOG] First ticket created with ID: " + response1.getTicketId());

        // Create second ticket (should the user queries be cached?)
        System.out.println("[DEBUG_LOG] Creating second ticket:");
        CreateTicketRequest request2 = new CreateTicketRequest(
                "Second Ticket",
                "Second ticket description",
                TicketCategory.PROGRAMS_TOOLS
        );
        TicketResponse response2 = ticketService.createTicket(request2, auth);
        System.out.println("[DEBUG_LOG] Second ticket created with ID: " + response2.getTicketId());

        // Verify both tickets were created
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotEquals(response1.getTicketId(), response2.getTicketId());
    }
}