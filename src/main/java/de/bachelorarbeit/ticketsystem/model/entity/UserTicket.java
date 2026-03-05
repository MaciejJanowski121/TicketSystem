package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * UserTicket entity representing the last time an end user viewed a ticket.
 * Uses a simple primary key with unique constraint on ticket and endUser.
 */
@Entity
@Table(
    name = "user_tickets",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ticket_id", "enduser_mail"})
    }
)
public class UserTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enduser_mail", referencedColumnName = "mail", nullable = false)
    private UserAccount endUser;

    @Column(nullable = false)
    private Instant lastViewed;

    // Default constructor required by JPA
    public UserTicket() {
    }

    public UserTicket(Ticket ticket, UserAccount endUser) {
        this.ticket = ticket;
        this.endUser = endUser;
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

    public UserAccount getEndUser() {
        return endUser;
    }

    public void setEndUser(UserAccount endUser) {
        this.endUser = endUser;
    }

    public Instant getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(Instant lastViewed) {
        this.lastViewed = lastViewed;
    }

    /**
     * Updates the lastViewed timestamp to the current time.
     */
    public void updateLastViewed() {
        this.lastViewed = Instant.now();
    }

    @Override
    public String toString() {
        return "UserTicket{" +
                "id=" + id +
                ", lastViewed=" + lastViewed +
                '}';
    }
}
