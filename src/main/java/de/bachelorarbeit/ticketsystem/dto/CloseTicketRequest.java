package de.bachelorarbeit.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for closing a ticket with a required closing comment.
 */
public class CloseTicketRequest {

    @NotBlank(message = "Closing comment is required")
    private String comment;

    // Default constructor
    public CloseTicketRequest() {
    }

    public CloseTicketRequest(String comment) {
        this.comment = comment;
    }

    // Getters and setters
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}