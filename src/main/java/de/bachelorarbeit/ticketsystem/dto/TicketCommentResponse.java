package de.bachelorarbeit.ticketsystem.dto;

import java.time.Instant;

/**
 * DTO for ticket comment responses in ticket details.
 */
public class TicketCommentResponse {

    private String comment;
    private Instant commentDate;
    private String authorUsername;

    // Default constructor
    public TicketCommentResponse() {
    }

    public TicketCommentResponse(String comment, Instant commentDate, String authorUsername) {
        this.comment = comment;
        this.commentDate = commentDate;
        this.authorUsername = authorUsername;
    }

    // Getters and setters
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Instant commentDate) {
        this.commentDate = commentDate;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }
}