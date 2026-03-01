package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.ErrorResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketCommentResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketListItemResponse;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
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

    /**
     * Get all tickets with optional filtering and sorting.
     * Accessible to any authenticated user (ENDUSER, SUPPORT, ADMIN).
     *
     * @param search optional search term to match title, description, creator username, or assigned support username
     * @param state optional state filter
     * @param category optional category filter
     * @param sort optional sort field (createDate or updateDate), defaults to updateDate
     * @param direction optional sort direction (ASC or DESC), defaults to DESC
     * @param authentication the authentication object containing current user info
     * @return list of tickets matching the criteria
     */
    @GetMapping
    @PreAuthorize("hasRole('ENDUSER') or hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<?> getAllTickets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketState state,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false, defaultValue = "updateDate") String sort,
            @RequestParam(required = false, defaultValue = "DESC") String direction,
            Authentication authentication) {
        try {
            List<TicketListItemResponse> tickets = ticketService.getAllTickets(
                    search, state, category, sort, direction, authentication);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving tickets"));
        }
    }

    /**
     * Get a specific ticket by ID for any authenticated user (read-only).
     *
     * @param ticketId the ID of the ticket to retrieve
     * @param authentication the authentication object containing current user info
     * @return the ticket details
     */
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasRole('ENDUSER') or hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<?> getTicketById(@PathVariable Long ticketId,
                                          Authentication authentication) {
        try {
            TicketResponse ticket = ticketService.getTicketById(ticketId, authentication);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Ticket not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving the ticket"));
        }
    }

    /**
     * Get all comments for a specific ticket.
     *
     * @param ticketId the ID of the ticket
     * @param authentication the authentication object containing current user info
     * @return list of comments for the ticket
     */
    @GetMapping("/{ticketId}/comments")
    @PreAuthorize("hasRole('ENDUSER') or hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<List<TicketCommentResponse>> getTicketComments(@PathVariable Long ticketId,
                                                                 Authentication authentication) {
        try {
            List<TicketCommentResponse> comments = ticketService.getTicketComments(ticketId, authentication);
            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Create a new comment on a ticket.
     *
     * @param ticketId the ID of the ticket
     * @param request the comment creation request
     * @param authentication the authentication object containing current user info
     * @return the created comment response with 201 Created status
     */
    @PostMapping("/{ticketId}/comments")
    @PreAuthorize("hasRole('ENDUSER') or hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<?> createTicketComment(@PathVariable Long ticketId,
                                                             @Valid @RequestBody CreateCommentRequest request,
                                                             Authentication authentication) {
        try {
            TicketCommentResponse comment = ticketService.createTicketComment(ticketId, request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}
