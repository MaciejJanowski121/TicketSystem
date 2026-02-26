package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for ticket operations.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Create a new ticket.
     *
     * @param request the ticket creation request
     * @param authentication the authentication object containing current user info
     * @return the created ticket response with 201 Created status
     */
    @PostMapping
    @PreAuthorize("hasRole('ENDUSER')")
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request, 
                                               Authentication authentication) {
        TicketResponse ticketResponse = ticketService.createTicket(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketResponse);
    }

    /**
     * Get all tickets for the current authenticated user.
     *
     * @param authentication the authentication object containing current user info
     * @return list of tickets created by the current user
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('ENDUSER')")
    public ResponseEntity<List<TicketResponse>> getMyTickets(Authentication authentication) {
        List<TicketResponse> tickets = ticketService.getMyTickets(authentication);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get a specific ticket by ID for the current authenticated user.
     *
     * @param ticketId the ID of the ticket to retrieve
     * @param authentication the authentication object containing current user info
     * @return the ticket details
     */
    @GetMapping("/my/{ticketId}")
    @PreAuthorize("hasRole('ENDUSER')")
    public ResponseEntity<TicketResponse> getMyTicketById(@PathVariable Long ticketId, 
                                                        Authentication authentication) {
        TicketResponse ticket = ticketService.getMyTicketById(ticketId, authentication);
        return ResponseEntity.ok(ticket);
    }
}
