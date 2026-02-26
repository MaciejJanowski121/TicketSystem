package de.bachelorarbeit.ticketsystem.dto;

import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new ticket.
 */
public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private TicketCategory ticketCategory;

    // Default constructor
    public CreateTicketRequest() {
    }

    public CreateTicketRequest(String title, String description, TicketCategory ticketCategory) {
        this.title = title;
        this.description = description;
        this.ticketCategory = ticketCategory;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketCategory getTicketCategory() {
        return ticketCategory;
    }

    public void setTicketCategory(TicketCategory ticketCategory) {
        this.ticketCategory = ticketCategory;
    }
}