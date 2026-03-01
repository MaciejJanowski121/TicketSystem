package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "support_ticket_assignment")
public class SupportTicketAssignment {

    @EmbeddedId
    private SupportTicketAssignmentPk sta_pk;

    @MapsId("ticketId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @MapsId("supportUserMail")
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
        this.sta_pk = new SupportTicketAssignmentPk(ticket.getTicketId(), supportUser.getMail());
        this.lastViewed = Instant.now();
    }

    public SupportTicketAssignmentPk getSta_pk() {
        return sta_pk;
    }

    public void setSta_pk(SupportTicketAssignmentPk sta_pk) {
        this.sta_pk = sta_pk;
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
                "sta_pk=" + sta_pk +
                ", lastViewed=" + lastViewed +
                '}';
    }
}