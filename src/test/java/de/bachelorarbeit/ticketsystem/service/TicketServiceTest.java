package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketServiceTest {

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
    void testCreateTicketWithUsername() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");

        // Create authentication object with username
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create ticket request
        CreateTicketRequest request = new CreateTicketRequest(
                "Hardware Issue",
                "My computer is not working properly",
                TicketCategory.HARDWARE
        );

        // Create ticket
        TicketResponse response = ticketService.createTicket(request, auth);

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getTicketId());
        assertEquals("Hardware Issue", response.getTitle());
        assertEquals("My computer is not working properly", response.getDescription());
        assertEquals(TicketCategory.HARDWARE, response.getTicketCategory());
        assertEquals(TicketState.UNASSIGNED, response.getTicketState());
        assertNotNull(response.getCreateDate());

        // Verify ticket was saved in database
        assertEquals(1, ticketRepository.count());
        Ticket savedTicket = ticketRepository.findAll().get(0);
        assertEquals("Hardware Issue", savedTicket.getTitle());
        assertEquals("testuser", savedTicket.getEndUser().getUsername());

        System.out.println("[DEBUG_LOG] Ticket created successfully with username authentication");
    }

    @Test
    void testCreateTicketWithEmail() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");

        // Create authentication object with email
        Authentication auth = new UsernamePasswordAuthenticationToken("test@example.com", null);

        // Create ticket request
        CreateTicketRequest request = new CreateTicketRequest(
                "Network Problem",
                "Cannot connect to the internet",
                TicketCategory.NETWORK
        );

        // Create ticket
        TicketResponse response = ticketService.createTicket(request, auth);

        // Verify response
        assertNotNull(response);
        assertEquals("Network Problem", response.getTitle());
        assertEquals("Cannot connect to the internet", response.getDescription());
        assertEquals(TicketCategory.NETWORK, response.getTicketCategory());
        assertEquals(TicketState.UNASSIGNED, response.getTicketState());

        // Verify ticket was saved in database
        assertEquals(1, ticketRepository.count());
        Ticket savedTicket = ticketRepository.findAll().get(0);
        assertEquals("test@example.com", savedTicket.getEndUser().getMail());

        System.out.println("[DEBUG_LOG] Ticket created successfully with email authentication");
    }

    @Test
    void testCreateTicketUserNotFoundByUsername() {
        // Create authentication object with non-existent username
        Authentication auth = new UsernamePasswordAuthenticationToken("nonexistent", null);

        // Create ticket request
        CreateTicketRequest request = new CreateTicketRequest(
                "Test Ticket",
                "Test Description",
                TicketCategory.OTHER
        );

        // Attempt to create ticket - should throw exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(request, auth)
        );

        assertEquals("User not found", exception.getMessage());
        assertEquals(0, ticketRepository.count());

        System.out.println("[DEBUG_LOG] User not found exception handled correctly for username");
    }

    @Test
    void testCreateTicketUserNotFoundByEmail() {
        // Create authentication object with non-existent email
        Authentication auth = new UsernamePasswordAuthenticationToken("nonexistent@example.com", null);

        // Create ticket request
        CreateTicketRequest request = new CreateTicketRequest(
                "Test Ticket",
                "Test Description",
                TicketCategory.ACCOUNT_MANAGEMENT
        );

        // Attempt to create ticket - should throw exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(request, auth)
        );

        assertEquals("User not found", exception.getMessage());
        assertEquals(0, ticketRepository.count());

        System.out.println("[DEBUG_LOG] User not found exception handled correctly for email");
    }

    @Test
    void testCreateTicketAllCategories() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Test all ticket categories
        TicketCategory[] categories = TicketCategory.values();
        for (TicketCategory category : categories) {
            CreateTicketRequest request = new CreateTicketRequest(
                    "Test " + category.name(),
                    "Description for " + category.getCategoryName(),
                    category
            );

            TicketResponse response = ticketService.createTicket(request, auth);
            assertEquals(category, response.getTicketCategory());
        }

        // Verify all tickets were created
        assertEquals(categories.length, ticketRepository.count());

        System.out.println("[DEBUG_LOG] All ticket categories tested successfully");
    }

    @Test
    void testTicketInitialState() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create ticket request
        CreateTicketRequest request = new CreateTicketRequest(
                "State Test",
                "Testing initial state",
                TicketCategory.PROGRAMS_TOOLS
        );

        // Create ticket
        TicketResponse response = ticketService.createTicket(request, auth);

        // Verify initial state is UNASSIGNED
        assertEquals(TicketState.UNASSIGNED, response.getTicketState());

        // Verify in database
        Ticket savedTicket = ticketRepository.findById(response.getTicketId()).orElse(null);
        assertNotNull(savedTicket);
        assertEquals(TicketState.UNASSIGNED, savedTicket.getTicketState());
        assertNull(savedTicket.getAssignedSupport());
        assertNull(savedTicket.getClosedDate());
        assertNotNull(savedTicket.getCreateDate());
        assertNotNull(savedTicket.getUpdateDate());

        System.out.println("[DEBUG_LOG] Ticket initial state verified correctly");
    }

    @Test
    void testGetMyTicketsEmpty() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Get tickets for user with no tickets
        List<TicketResponse> tickets = ticketService.getMyTickets(auth);

        // Verify empty list
        assertNotNull(tickets);
        assertTrue(tickets.isEmpty());

        System.out.println("[DEBUG_LOG] Empty ticket list returned correctly");
    }

    @Test
    void testGetMyTicketsWithMultipleTickets() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create multiple tickets
        CreateTicketRequest request1 = new CreateTicketRequest(
                "First Ticket",
                "First description",
                TicketCategory.HARDWARE
        );
        CreateTicketRequest request2 = new CreateTicketRequest(
                "Second Ticket",
                "Second description",
                TicketCategory.NETWORK
        );
        CreateTicketRequest request3 = new CreateTicketRequest(
                "Third Ticket",
                "Third description",
                TicketCategory.OTHER
        );

        TicketResponse ticket1 = ticketService.createTicket(request1, auth);
        TicketResponse ticket2 = ticketService.createTicket(request2, auth);
        TicketResponse ticket3 = ticketService.createTicket(request3, auth);

        // Get all tickets
        List<TicketResponse> tickets = ticketService.getMyTickets(auth);

        // Verify all tickets are returned
        assertNotNull(tickets);
        assertEquals(3, tickets.size());

        // Verify tickets are sorted by updateDate descending (newest first)
        // Since we created them in sequence, the last created should be first
        assertEquals("Third Ticket", tickets.get(0).getTitle());
        assertEquals("Second Ticket", tickets.get(1).getTitle());
        assertEquals("First Ticket", tickets.get(2).getTitle());

        System.out.println("[DEBUG_LOG] Multiple tickets retrieved and sorted correctly");
    }

    @Test
    void testGetMyTicketsUserNotFoundByUsername() {
        // Create authentication object with non-existent username
        Authentication auth = new UsernamePasswordAuthenticationToken("nonexistent", null);

        // Attempt to get tickets - should throw exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getMyTickets(auth)
        );

        assertEquals("User not found", exception.getMessage());

        System.out.println("[DEBUG_LOG] User not found exception handled correctly in getMyTickets with username");
    }

    @Test
    void testGetMyTicketsUserNotFoundByEmail() {
        // Create authentication object with non-existent email
        Authentication auth = new UsernamePasswordAuthenticationToken("nonexistent@example.com", null);

        // Attempt to get tickets - should throw exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getMyTickets(auth)
        );

        assertEquals("User not found", exception.getMessage());

        System.out.println("[DEBUG_LOG] User not found exception handled correctly in getMyTickets with email");
    }

    @Test
    void testGetMyTicketByIdSuccess() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Create a ticket
        CreateTicketRequest request = new CreateTicketRequest(
                "Test Ticket",
                "Test description",
                TicketCategory.PROGRAMS_TOOLS
        );
        TicketResponse createdTicket = ticketService.createTicket(request, auth);

        // Get the ticket by ID
        TicketResponse retrievedTicket = ticketService.getMyTicketById(createdTicket.getTicketId(), auth);

        // Verify the ticket details
        assertNotNull(retrievedTicket);
        assertEquals(createdTicket.getTicketId(), retrievedTicket.getTicketId());
        assertEquals("Test Ticket", retrievedTicket.getTitle());
        assertEquals("Test description", retrievedTicket.getDescription());
        assertEquals(TicketCategory.PROGRAMS_TOOLS, retrievedTicket.getTicketCategory());
        assertEquals(TicketState.UNASSIGNED, retrievedTicket.getTicketState());

        System.out.println("[DEBUG_LOG] Ticket retrieved by ID successfully");
    }

    @Test
    void testGetMyTicketByIdNotFound() {
        // Register a user
        authService.register("testuser", "test@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        // Attempt to get non-existent ticket
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getMyTicketById(999L, auth)
        );

        assertEquals("Ticket not found", exception.getMessage());

        System.out.println("[DEBUG_LOG] Ticket not found exception handled correctly");
    }

    @Test
    void testGetMyTicketByIdAccessDenied() {
        // Register two users
        authService.register("user1", "user1@example.com", "password123");
        authService.register("user2", "user2@example.com", "password123");

        Authentication auth1 = new UsernamePasswordAuthenticationToken("user1", null);
        Authentication auth2 = new UsernamePasswordAuthenticationToken("user2", null);

        // Create a ticket with user1
        CreateTicketRequest request = new CreateTicketRequest(
                "User1 Ticket",
                "User1 description",
                TicketCategory.ACCOUNT_MANAGEMENT
        );
        TicketResponse ticket = ticketService.createTicket(request, auth1);

        // Attempt to access the ticket with user2 - should throw exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getMyTicketById(ticket.getTicketId(), auth2)
        );

        assertEquals("Access denied: Ticket does not belong to current user", exception.getMessage());

        System.out.println("[DEBUG_LOG] Access denied exception handled correctly");
    }

    @Test
    void testGetMyTicketByIdUserNotFound() {
        // Register a user and create a ticket
        authService.register("testuser", "test@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);

        CreateTicketRequest request = new CreateTicketRequest(
                "Test Ticket",
                "Test description",
                TicketCategory.OTHER
        );
        TicketResponse ticket = ticketService.createTicket(request, auth);

        // Create authentication with non-existent user
        Authentication invalidAuth = new UsernamePasswordAuthenticationToken("nonexistent", null);

        // Attempt to get ticket with invalid user
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getMyTicketById(ticket.getTicketId(), invalidAuth)
        );

        assertEquals("User not found", exception.getMessage());

        System.out.println("[DEBUG_LOG] User not found exception handled correctly in getMyTicketById");
    }

    @Test
    void testGetMyTicketsWithNullAuthentication() {
        // Test defensive check for null authentication
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.getMyTickets(null)
        );

        assertEquals("Authentication is required", exception.getMessage());

        System.out.println("[DEBUG_LOG] Null authentication properly handled: " + exception.getMessage());
    }
}
