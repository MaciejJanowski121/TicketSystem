package de.bachelorarbeit.ticketsystem.dto;

import java.time.Instant;

/**
 * DTO for comment responses.
 */
public class CommentResponse {

    private Long ticketId;
    private String commentUserMail;
    private String commentUserName;
    private Instant commentDate;
    private String comment;

    // Default constructor
    public CommentResponse() {
    }

    public CommentResponse(Long ticketId, String commentUserMail, String commentUserName, 
                          Instant commentDate, String comment) {
        this.ticketId = ticketId;
        this.commentUserMail = commentUserMail;
        this.commentUserName = commentUserName;
        this.commentDate = commentDate;
        this.comment = comment;
    }

    // Getters and setters
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getCommentUserMail() {
        return commentUserMail;
    }

    public void setCommentUserMail(String commentUserMail) {
        this.commentUserMail = commentUserMail;
    }

    public String getCommentUserName() {
        return commentUserName;
    }

    public void setCommentUserName(String commentUserName) {
        this.commentUserName = commentUserName;
    }

    public Instant getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Instant commentDate) {
        this.commentDate = commentDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}