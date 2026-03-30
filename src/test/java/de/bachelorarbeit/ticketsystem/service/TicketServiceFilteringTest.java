package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.TicketListItemResponse;
import de.bachelorarbeit.ticketsystem.model.entity.*;
import de.bachelorarbeit.ticketsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketService filtering and search functionality.
 * Uses Mockito for repository mocking as requested in the issue.
 */
@ExtendWith(MockitoExtension.class)
public class TicketServiceFilteringTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Mock
    private UserTicketRepository userTicketRepository;

    @Mock
    private SupportTicketAssignmentRepository supportTicketAssignmentRepository;

    @InjectMocks
    private TicketService ticketService;

    private UserAccount testUser;
    private Ticket testTicket1;
    private Ticket testTicket2;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new UserAccount("testuser", "test@example.com", "password", Role.ENDUSER);

        // Create test tickets
        testTicket1 = new Ticket();
        testTicket1.setTicketId(1L);
        testTicket1.setTitle("Hardware Issue");
        testTicket1.setDescription("Computer not working");
        testTicket1.setTicketState(TicketState.UNASSIGNED);
        testTicket1.setTicketCategory(TicketCategory.HARDWARE);
        testTicket1.setEndUser(testUser);
        testTicket1.setCreateDate(Instant.now().minusSeconds(3600));
        testTicket1.setUpdateDate(Instant.now().minusSeconds(1800));

        testTicket2 = new Ticket();
        testTicket2.setTicketId(2L);
        testTicket2.setTitle("Network Problem");
        testTicket2.setDescription("Internet connection issues");
        testTicket2.setTicketState(TicketState.IN_PROGRESS);
        testTicket2.setTicketCategory(TicketCategory.NETWORK);
        testTicket2.setEndUser(testUser);
        testTicket2.setCreateDate(Instant.now().minusSeconds(7200));
        testTicket2.setUpdateDate(Instant.now().minusSeconds(900));

        authentication = new UsernamePasswordAuthenticationToken("test@example.com", null);
    }

    // ========== FILTER BY STATE TESTS ==========

    @Test
    void testGetAllTickets_FilterByState_UNASSIGNED() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(TicketState.UNASSIGNED, null))
                .thenReturn(Arrays.asList(testTicket1));

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                null, TicketState.UNASSIGNED, null, "updateDate", "DESC", authentication);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Hardware Issue", result.get(0).getTitle());
        assertEquals(TicketState.UNASSIGNED, result.get(0).getTicketState());

        verify(ticketRepository).findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(TicketState.UNASSIGNED, null);
        verify(userRepository).findByMail("test@example.com");

        System.out.println("[DEBUG_LOG] Filter by state UNASSIGNED test passed");
    }

    @Test
    void testGetAllTickets_FilterByCategory_HARDWARE() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(null, TicketCategory.HARDWARE))
                .thenReturn(Arrays.asList(testTicket1));

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                null, null, TicketCategory.HARDWARE, "updateDate", "DESC", authentication);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Hardware Issue", result.get(0).getTitle());
        assertEquals(TicketCategory.HARDWARE, result.get(0).getTicketCategory());

        verify(ticketRepository).findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(null, TicketCategory.HARDWARE);

        System.out.println("[DEBUG_LOG] Filter by category HARDWARE test passed");
    }

    @Test
    void testGetAllTickets_CombinedFilters_StateAndCategory() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(TicketState.UNASSIGNED, TicketCategory.HARDWARE))
                .thenReturn(Arrays.asList(testTicket1));

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                null, TicketState.UNASSIGNED, TicketCategory.HARDWARE, "updateDate", "DESC", authentication);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Hardware Issue", result.get(0).getTitle());
        assertEquals(TicketState.UNASSIGNED, result.get(0).getTicketState());
        assertEquals(TicketCategory.HARDWARE, result.get(0).getTicketCategory());

        verify(ticketRepository).findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(TicketState.UNASSIGNED, TicketCategory.HARDWARE);

        System.out.println("[DEBUG_LOG] Combined filters (state + category) test passed");
    }

    @Test
    void testGetAllTickets_SearchByTitle() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findTicketsWithFiltersOrderByUpdateDateDesc("hardware", null, null))
                .thenReturn(Arrays.asList(testTicket1));

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                "hardware", null, null, "updateDate", "DESC", authentication);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Hardware Issue", result.get(0).getTitle());

        verify(ticketRepository).findTicketsWithFiltersOrderByUpdateDateDesc("hardware", null, null);

        System.out.println("[DEBUG_LOG] Search by title test passed");
    }

    @Test
    void testGetAllTickets_SearchPartialMatch() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findTicketsWithFiltersOrderByUpdateDateDesc("net", null, null))
                .thenReturn(Arrays.asList(testTicket2));

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                "net", null, null, "updateDate", "DESC", authentication);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Network Problem", result.get(0).getTitle());

        verify(ticketRepository).findTicketsWithFiltersOrderByUpdateDateDesc("net", null, null);

        System.out.println("[DEBUG_LOG] Search partial match test passed");
    }

    @Test
    void testGetAllTickets_SortByCreateDate_ASC() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findAllByOrderByCreateDateAsc())
                .thenReturn(Arrays.asList(testTicket2, testTicket1)); // testTicket2 created earlier

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                null, null, null, "createDate", "ASC", authentication);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Network Problem", result.get(0).getTitle()); // Earlier created ticket first
        assertEquals("Hardware Issue", result.get(1).getTitle());

        verify(ticketRepository).findAllByOrderByCreateDateAsc();

        System.out.println("[DEBUG_LOG] Sort by createDate ASC test passed");
    }

    @Test
    void testGetAllTickets_SortByUpdateDate_DESC() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findAllByOrderByUpdateDateDesc())
                .thenReturn(Arrays.asList(testTicket2, testTicket1)); // testTicket2 updated later

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                null, null, null, "updateDate", "DESC", authentication);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Network Problem", result.get(0).getTitle()); // Later updated ticket first
        assertEquals("Hardware Issue", result.get(1).getTitle());

        verify(ticketRepository).findAllByOrderByUpdateDateDesc();

        System.out.println("[DEBUG_LOG] Sort by updateDate DESC test passed");
    }

    @Test
    void testGetAllTickets_DefaultSorting_NullSort() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findAllByOrderByUpdateDateDesc())
                .thenReturn(Arrays.asList(testTicket2, testTicket1));

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                null, null, null, null, "DESC", authentication);

        // Assert
        assertEquals(2, result.size());
        verify(ticketRepository).findAllByOrderByUpdateDateDesc(); // Should default to updateDate

        System.out.println("[DEBUG_LOG] Default sorting (null sort) test passed");
    }

    @Test
    void testGetAllTickets_DefaultDirection_InvalidDirection() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findAllByOrderByUpdateDateDesc())
                .thenReturn(Arrays.asList(testTicket2, testTicket1));

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                null, null, null, "updateDate", "INVALID", authentication);

        // Assert
        assertEquals(2, result.size());
        verify(ticketRepository).findAllByOrderByUpdateDateDesc(); // Should default to DESC

        System.out.println("[DEBUG_LOG] Default direction (invalid direction) test passed");
    }

    @Test
    void testGetAllTickets_EmptyResult() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findTicketsWithFiltersOrderByUpdateDateDesc("nonexistent", null, null))
                .thenReturn(Collections.emptyList());

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                "nonexistent", null, null, "updateDate", "DESC", authentication);

        // Assert
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());

        verify(ticketRepository).findTicketsWithFiltersOrderByUpdateDateDesc("nonexistent", null, null);

        System.out.println("[DEBUG_LOG] Empty result test passed");
    }

    @Test
    void testGetAllTickets_NoMatches_CombinedFilters() {
        // Arrange
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));
        when(ticketRepository.findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(TicketState.CLOSED, TicketCategory.OTHER))
                .thenReturn(Collections.emptyList());

        // Act
        List<TicketListItemResponse> result = ticketService.getAllTickets(
                null, TicketState.CLOSED, TicketCategory.OTHER, "updateDate", "DESC", authentication);

        // Assert
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());

        verify(ticketRepository).findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(TicketState.CLOSED, TicketCategory.OTHER);

        System.out.println("[DEBUG_LOG] No matches with combined filters test passed");
    }
}
