package de.bachelorarbeit.ticketsystem.repository;

import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to validate the new repository methods and JPQL queries.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketRepositoryValidationTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void testRepositoryLoads() {
        // This test will fail if there are JPQL syntax errors
        assertNotNull(ticketRepository);
        System.out.println("[DEBUG_LOG] TicketRepository loaded successfully");
    }

    @Test
    void testBasicQuery() {
        // Test the basic findTicketsWithFiltersOrderByUpdateDateDesc method
        var tickets = ticketRepository.findTicketsWithFiltersOrderByUpdateDateDesc(null, null, null);
        assertNotNull(tickets);
        System.out.println("[DEBUG_LOG] Basic query executed successfully");
    }

    @Test
    void testSearchQuery() {
        // Test the search query method
        var tickets = ticketRepository.findTicketsWithFiltersOrderByUpdateDateDesc("test", null, null);
        assertNotNull(tickets);
        System.out.println("[DEBUG_LOG] Search query executed successfully");
    }
}
