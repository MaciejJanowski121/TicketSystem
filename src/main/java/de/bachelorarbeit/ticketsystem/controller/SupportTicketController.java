package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.ErrorResponse;
import de.bachelorarbeit.ticketsystem.dto.SupportTicketUpdateRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketListItemResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
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
 * Controller for support ticket operations.
 * Handles support workflow endpoints under /api/support/tickets.
 */
@RestController
@RequestMapping("/api/support/tickets")
public class SupportTicketController {

    private final TicketService ticketService;

    public SupportTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Get tickets assigned to the current support user.
     *
     * @param search optional search term to match title, description, or creator username
     * @param state optional state filter
     * @param category optional category filter
     * @param sort optional sort field (createDate or updateDate), defaults to updateDate
     * @param direction optional sort direction (ASC or DESC), defaults to DESC
     * @param authentication the authentication object containing current user info
     * @return list of tickets assigned to current support user
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<?> getMySupportTickets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketState state,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false, defaultValue = "updateDate") String sort,
            @RequestParam(required = false, defaultValue = "DESC") String direction,
            Authentication authentication) {
        try {
            List<TicketListItemResponse> tickets = ticketService.getMySupportTickets(
                    search, state, category, sort, direction, authentication);
            return ResponseEntity.ok(tickets);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while retrieving support tickets"));
        }
    }

    /**
     * Assign a ticket to the current support user.
     *
     * @param ticketId the ID of the ticket to assign
     * @param authentication the authentication object containing current user info
     * @return the updated ticket response
     */
    @PostMapping("/{ticketId}/assign")
    @PreAuthorize("hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<?> assignTicket(@PathVariable Long ticketId,
                                         Authentication authentication) {
        try {
            TicketResponse ticket = ticketService.assignTicketToCurrentSupport(ticketId, authentication);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage()));
            } else if (e.getMessage().contains("already assigned") || e.getMessage().contains("closed")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while assigning the ticket"));
        }
    }

    /**
     * Release a ticket from the current support user.
     *
     * @param ticketId the ID of the ticket to release
     * @param authentication the authentication object containing current user info
     * @return the updated ticket response
     */
    @PostMapping("/{ticketId}/release")
    @PreAuthorize("hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<?> releaseTicket(@PathVariable Long ticketId,
                                          Authentication authentication) {
        try {
            TicketResponse ticket = ticketService.releaseTicket(ticketId, authentication);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while releasing the ticket"));
        }
    }

    /**
     * Update ticket status and/or category.
     *
     * @param ticketId the ID of the ticket to update
     * @param request the update request containing optional state and category
     * @param authentication the authentication object containing current user info
     * @return the updated ticket response
     */
    @PatchMapping("/{ticketId}")
    @PreAuthorize("hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<?> updateTicket(@PathVariable Long ticketId,
                                         @Valid @RequestBody SupportTicketUpdateRequest request,
                                         Authentication authentication) {
        try {
            TicketResponse ticket = ticketService.updateSupportTicket(ticketId, request, authentication);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage()));
            } else if (e.getMessage().contains("Assign ticket first")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating the ticket"));
        }
    }

    /**
     * Close a ticket (shortcut endpoint).
     *
     * @param ticketId the ID of the ticket to close
     * @param authentication the authentication object containing current user info
     * @return the updated ticket response
     */
    @PostMapping("/{ticketId}/close")
    @PreAuthorize("hasRole('SUPPORTUSER') or hasRole('ADMINUSER')")
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId,
                                        Authentication authentication) {
        try {
            TicketResponse ticket = ticketService.closeTicket(ticketId, authentication);
            return ResponseEntity.ok(ticket);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while closing the ticket"));
        }
    }
}