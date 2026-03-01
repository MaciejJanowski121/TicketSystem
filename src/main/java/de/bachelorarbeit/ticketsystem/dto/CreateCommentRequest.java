package de.bachelorarbeit.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new comment on a ticket.
 */
public class CreateCommentRequest {

    @NotBlank(message = "Comment is required")
    private String comment;

    // Default constructor
    public CreateCommentRequest() {
    }

    public CreateCommentRequest(String comment) {
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