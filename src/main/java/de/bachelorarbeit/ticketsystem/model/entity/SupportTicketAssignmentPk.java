package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SupportTicketAssignmentPk implements Serializable {

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "support_user_mail")
    private String supportUserMail;

    // JPA
    protected SupportTicketAssignmentPk() {
    }

    public SupportTicketAssignmentPk(Long ticketId, String supportUserMail) {
        this.ticketId = ticketId;
        this.supportUserMail = supportUserMail;
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

    public void setSupportUserMail(String supportUserMail) {
        this.supportUserMail = supportUserMail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupportTicketAssignmentPk)) return false;
        SupportTicketAssignmentPk that = (SupportTicketAssignmentPk) o;
        return Objects.equals(ticketId, that.ticketId) &&
                Objects.equals(supportUserMail, that.supportUserMail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, supportUserMail);
    }

    @Override
    public String toString() {
        return "SupportTicketAssignmentPk{" +
                "ticketId=" + ticketId +
                ", supportUserMail='" + supportUserMail + '\'' +
                '}';
    }
}