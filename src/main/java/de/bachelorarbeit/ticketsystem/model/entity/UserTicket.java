package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * UserTicket entity representing the last time an end user viewed a ticket.
 * Uses a composite primary key consisting of ticket and endUser.
 */
@Entity
@Table(name = "user_tickets")
public class UserTicket {

    @EmbeddedId
    private UserTicketPk id;

    @MapsId("ticketId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enduser_email", referencedColumnName = "mail", insertable = false, updatable = false)
    private UserAccount endUser;

    @Column(nullable = false)
    private Instant lastViewed;

    // Default constructor required by JPA
    public UserTicket() {
    }

    public UserTicket(Ticket ticket, UserAccount endUser) {
        this.ticket = ticket;
        this.endUser = endUser;
        this.id = new UserTicketPk(ticket.getTicketId(), endUser.getMail());
        this.lastViewed = Instant.now();
    }

    // Getters and setters

    public UserTicketPk getId() {
        return id;
    }

    public void setId(UserTicketPk id) {
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
