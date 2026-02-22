package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * TicketComment entity representing a comment on a ticket.
 * Uses a composite primary key consisting of ticket, commentUser, and commentDate.
 */
@Entity
@Table(name = "ticket_comment")
public class TicketComment {

    @EmbeddedId
    private TicketCommentPk tc_pk;

    @MapsId("ticketId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_user_mail", referencedColumnName = "mail", insertable = false, updatable = false)
    private UserAccount commentUser;

    @Column(nullable = false)
    private String comment;

    // Default constructor required by JPA
    public TicketComment() {
    }

    public TicketComment(Ticket ticket, UserAccount commentUser, String comment) {
        this.ticket = ticket;
        this.commentUser = commentUser;
        this.comment = comment;
        this.tc_pk = new TicketCommentPk(ticket.getTicketId(), commentUser.getMail(), Instant.now());
    }

    // Getters and setters

    public TicketCommentPk getTc_pk() {
        return tc_pk;
    }

    public void setTc_pk(TicketCommentPk tc_pk) {
        this.tc_pk = tc_pk;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public UserAccount getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(UserAccount commentUser) {
        this.commentUser = commentUser;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCommentDate() {
        return tc_pk.getCommentDate();
    }

    @Override
    public String toString() {
        return "TicketComment{" +
                "tc_pk=" + tc_pk +
                ", comment='" + comment + '\'' +
                '}';
    }
}
