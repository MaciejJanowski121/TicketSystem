package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Embeddable composite key for TicketComment entity.
 * Consists of ticketId, commentUserMail, and commentDate.
 */
@Embeddable
public class TicketCommentPk implements Serializable {

    @Column(name = "ticket_id")
    private Long ticketId;
    @Column(name = "comment_email")
    private String commentUserMail;
    @Column(name = "comment_date")
    private Instant commentDate;

    // Default constructor required by JPA
    public TicketCommentPk() {
    }

    public TicketCommentPk(Long ticketId, String commentUserMail, Instant commentDate) {
        this.ticketId = ticketId;
        this.commentUserMail = commentUserMail;
        this.commentDate = commentDate;
    }

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
                Objects.equals(commentUserMail, that.commentUserMail) &&
                Objects.equals(commentDate, that.commentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, commentUserMail, commentDate);
    }

    @Override
    public String toString() {
        return "TicketCommentId{" +
                "ticketId=" + ticketId +
                ", commentUserMail='" + commentUserMail + '\'' +
                ", commentDate=" + commentDate +
                '}';
    }
}
