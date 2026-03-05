package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * SupportTicketAssignment entity representing the assignment of a support user to a ticket.
 * Uses a simple primary key with unique constraint on ticket and supportUser.
 */
@Entity
@Table(
    name = "support_ticket_assignment",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ticket_id", "support_user_mail"})
    }
)
public class SupportTicketAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "support_user_mail", referencedColumnName = "mail", nullable = false)
    private UserAccount supportUser;

    @Column(name = "last_viewed", nullable = false)
    private Instant lastViewed;

    // JPA
    protected SupportTicketAssignment() {
    }

    public SupportTicketAssignment(Ticket ticket, UserAccount supportUser) {
        this.ticket = ticket;
        this.supportUser = supportUser;
        this.lastViewed = Instant.now();
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

    public UserAccount getSupportUser() {
        return supportUser;
    }

    public void setSupportUser(UserAccount supportUser) {
        this.supportUser = supportUser;
    }

    public Instant getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(Instant lastViewed) {
        this.lastViewed = lastViewed;
    }

    public void updateLastViewed() {
        this.lastViewed = Instant.now();
    }

    @Override
    public String toString() {
        return "SupportTicketAssignment{" +
                "id=" + id +
                ", lastViewed=" + lastViewed +
                '}';
    }
}
