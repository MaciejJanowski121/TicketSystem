package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * SupportTicketAssignment entity representing the assignment of a ticket to a support user.
 * Uses a composite primary key consisting of ticket and support user.
 */
@Entity
@Table(name = "support_ticket_assignment")
public class SupportTicketAssignment {

    @EmbeddedId
    private SupportTicketAssignmentPk sta_pk;

    @MapsId("ticketId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_mail", referencedColumnName = "mail", insertable = false, updatable = false)
    private UserAccount supportUser;

    @Column(nullable = false)
    private Instant lastViewed;

    // Default constructor required by JPA
    public SupportTicketAssignment() {
    }

    public SupportTicketAssignment(Ticket ticket, UserAccount supportUser) {
        this.ticket = ticket;
        this.supportUser = supportUser;
        this.sta_pk = new SupportTicketAssignmentPk(ticket.getTicketId(), supportUser.getMail());
        this.lastViewed = Instant.now();
    }

    // Getters and setters

    public SupportTicketAssignmentPk getId() {
        return sta_pk;
    }

    public void setId(SupportTicketAssignmentPk id) {
        this.sta_pk = id;
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

    /**
     * Updates the lastViewed timestamp to the current time.
     */
    public void updateLastViewed() {
        this.lastViewed = Instant.now();
    }

    @Override
    public String toString() {
        return "SupportTicketAssignment{" +
                "pk=" + sta_pk +
                ", lastViewed=" + lastViewed +
                '}';
    }
}
