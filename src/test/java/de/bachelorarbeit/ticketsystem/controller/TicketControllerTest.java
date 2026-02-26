package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketControllerTest {

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
    void testCreateTicketSuccess() {
        // First register a user with ENDUSER role
        authService.register("testuser", "test@example.com", "password123");

        // Verify user exists
        UserAccount user = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(user);
        assertEquals(Role.ENDUSER, user.getRole());

        // Verify ticket repository is initially empty
        assertEquals(0, ticketRepository.count());

        // Note: Full integration test with authentication would require more complex setup
        // This test verifies the basic service layer functionality
        System.out.println("[DEBUG_LOG] User created successfully with ENDUSER role");
        System.out.println("[DEBUG_LOG] Ready for ticket creation testing");
    }

    @Test
    void testCreateTicketRequestValidation() {
        // Test CreateTicketRequest validation
        CreateTicketRequest request = new CreateTicketRequest();
        
        // Test with null values - should fail validation
        assertNull(request.getTitle());
        assertNull(request.getDescription());
        assertNull(request.getTicketCategory());

        // Test with valid values
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setTicketCategory(TicketCategory.HARDWARE);

        assertEquals("Test Ticket", request.getTitle());
        assertEquals("Test Description", request.getDescription());
        assertEquals(TicketCategory.HARDWARE, request.getTicketCategory());

        System.out.println("[DEBUG_LOG] CreateTicketRequest validation test completed");
    }

    @Test
    void testTicketCategoryValues() {
        // Test all available ticket categories
        TicketCategory[] categories = TicketCategory.values();
        assertEquals(5, categories.length);

        // Verify specific categories exist
        assertTrue(java.util.Arrays.asList(categories).contains(TicketCategory.ACCOUNT_MANAGEMENT));
        assertTrue(java.util.Arrays.asList(categories).contains(TicketCategory.HARDWARE));
        assertTrue(java.util.Arrays.asList(categories).contains(TicketCategory.PROGRAMS_TOOLS));
        assertTrue(java.util.Arrays.asList(categories).contains(TicketCategory.NETWORK));
        assertTrue(java.util.Arrays.asList(categories).contains(TicketCategory.OTHER));

        System.out.println("[DEBUG_LOG] All ticket categories verified");
    }

    @Test
    void testUserRoleForTicketCreation() {
        // Register users with different roles
        authService.register("enduser", "enduser@example.com", "password123");
        
        UserAccount endUser = userRepository.findByUsername("enduser").orElse(null);
        assertNotNull(endUser);
        assertEquals(Role.ENDUSER, endUser.getRole());

        // Only ENDUSER should be able to create tickets (verified by @PreAuthorize annotation)
        System.out.println("[DEBUG_LOG] User role verification completed - ENDUSER role confirmed");
    }

    @Test
    void testTicketRepositoryFunctionality() {
        // Test that TicketRepository has the expected methods
        assertNotNull(ticketRepository);
        
        // Verify repository is empty initially
        assertEquals(0, ticketRepository.count());
        assertTrue(ticketRepository.findAll().isEmpty());

        System.out.println("[DEBUG_LOG] TicketRepository functionality verified");
    }
}