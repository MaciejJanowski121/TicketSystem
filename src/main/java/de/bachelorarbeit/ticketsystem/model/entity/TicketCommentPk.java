package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Embeddable composite key for TicketComment entity.
 * Consists of ticketId, commentEmail, and commentDate.
 */
@Embeddable
public class TicketCommentPk implements Serializable {

    @Column(name = "ticket_id")
    private Long ticketId;
    @Column(name = "comment_email")
    private String commentEmail;
    @Column(name = "comment_date")
    private Instant commentDate;

    // Default constructor required by JPA
    public TicketCommentPk() {
    }

    public TicketCommentPk(Long ticketId, String commentEmail, Instant commentDate) {
        this.ticketId = ticketId;
        this.commentEmail = commentEmail;
        this.commentDate = commentDate;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }


    public String getCommentEmail() {
        return commentEmail;
    }

    public void setCommentEmail(String commentEmail) {
        this.commentEmail = commentEmail;
    }

    public Instant getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Instant commentDate) {
        this.commentDate = commentDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketCommentPk that = (TicketCommentPk) o;
        return Objects.equals(ticketId, that.ticketId) &&
                Objects.equals(commentEmail, that.commentEmail) &&
                Objects.equals(commentDate, that.commentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, commentEmail, commentDate);
    }

    @Override
    public String toString() {
        return "TicketCommentId{" +
                "ticketId=" + ticketId +
                ", commentEmail='" + commentEmail + '\'' +
                ", commentDate=" + commentDate +
                '}';
    }
}
