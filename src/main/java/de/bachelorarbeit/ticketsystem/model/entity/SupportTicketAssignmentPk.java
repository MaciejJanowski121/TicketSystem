package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Embeddable composite key for SupportTicketAssignment entity.
 * Consists of ticketId and supportEmail.
 */
@Embeddable
public class SupportTicketAssignmentPk implements Serializable {

    @Column(name = "ticket_id")
    private Long ticketId;
    @Column(name = "support_user_mail")
    private String supportUserMail;

    // Default constructor required by JPA
    public SupportTicketAssignmentPk() {
    }

    public SupportTicketAssignmentPk(Long ticketId, String supportMail) {
        this.ticketId = ticketId;
        this.supportUserMail = supportMail;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getSupportUserMail() {
        return supportUserMail;
    }

    public void setSupportUserMail(String supportEmail) {
        this.supportUserMail = supportEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupportTicketAssignmentPk that = (SupportTicketAssignmentPk) o;
        return Objects.equals(ticketId, that.ticketId) && Objects.equals(supportUserMail, that.supportUserMail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, supportUserMail);
    }

    @Override
    public String toString() {
        return "SupportTicketAssignmentId{" + "ticketId=" + ticketId + ", supportEmail='" + supportUserMail + '\'' + '}';
    }
}
