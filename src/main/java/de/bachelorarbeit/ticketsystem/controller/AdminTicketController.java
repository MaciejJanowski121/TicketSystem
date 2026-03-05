package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.ErrorResponse;
import de.bachelorarbeit.ticketsystem.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for admin ticket operations.
 * Handles admin-specific ticket endpoints under /api/admin/tickets.
 */
@RestController
@RequestMapping("/api/admin/tickets")
public class AdminTicketController {

    private final TicketService ticketService;

    public AdminTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Delete a ticket and all related data.
     * Performs cascading delete of:
     * - SupportTicketAssignment for that ticket (if exists)
     * - UserTicket rows for that ticket (if exist)
     * - TicketComment rows for that ticket (if exist)
     * - The Ticket itself
     *
     * @param ticketId the ID of the ticket to delete
     * @param authentication the authentication object containing current user info
     * @return 204 No Content on success, 404 if ticket not found
     */
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('ADMINUSER')")
    public ResponseEntity<?> deleteTicket(@PathVariable Long ticketId,
                                         Authentication authentication) {
        try {
            ticketService.deleteTicket(ticketId, authentication);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Ticket not found"));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while deleting the ticket"));
        }
    }
}