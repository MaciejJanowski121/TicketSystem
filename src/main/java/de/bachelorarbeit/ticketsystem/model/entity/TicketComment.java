package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * TicketComment entity representing a comment on a ticket.
 * Uses a simple primary key with unique constraint on ticket, commentUser, and commentDate.
 */
@Entity
@Table(
    name = "ticket_comment",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ticket_id", "comment_user_mail", "comment_date"})
    }
)
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_user_mail", referencedColumnName = "mail", nullable = false)
    private UserAccount commentUser;

    @Column(name = "comment_date", nullable = false)
    private Instant commentDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    // Default constructor required by JPA
    public TicketComment() {
    }

    public TicketComment(Ticket ticket, UserAccount commentUser, String comment) {
        this.ticket = ticket;
        this.commentUser = commentUser;
        this.comment = comment;
        this.commentDate = Instant.now();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return commentDate;
    }



    @Override
    public String toString() {
        return "TicketComment{" +
                "id=" + id +
                ", commentDate=" + commentDate +
                ", comment='" + comment + '\'' +
                '}';
    }
}
