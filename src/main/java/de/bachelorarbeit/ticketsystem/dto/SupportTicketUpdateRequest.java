package de.bachelorarbeit.ticketsystem.dto;

import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;

/**
 * DTO for support ticket update requests.
 * Contains optional fields for updating ticket state and category.
 */
public class SupportTicketUpdateRequest {

    private TicketState ticketState;
    private TicketCategory ticketCategory;

    // Default constructor
    public SupportTicketUpdateRequest() {
    }

    public SupportTicketUpdateRequest(TicketState ticketState, TicketCategory ticketCategory) {
        this.ticketState = ticketState;
        this.ticketCategory = ticketCategory;
    }

    // Getters and setters
    public TicketState getTicketState() {
        return ticketState;
    }

    public void setTicketState(TicketState ticketState) {
        this.ticketState = ticketState;
    }

    public TicketCategory getTicketCategory() {
        return ticketCategory;
    }

    public void setTicketCategory(TicketCategory ticketCategory) {
        this.ticketCategory = ticketCategory;
    }
}