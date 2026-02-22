package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Embeddable composite key for UserTicket entity.
 * Consists of ticketId and endUserMail.
 */
@Embeddable
public class UserTicketPk implements Serializable {

    @Column(name = "ticket_id")
    private Long ticketId;
    @Column(name = "enduser_mail")
    private String endUserMail;

    // Default constructor required by JPA
    public UserTicketPk() {
    }

    public UserTicketPk(Long ticketId, String endUserMail) {
        this.ticketId = ticketId;
        this.endUserMail = endUserMail;
    }

    public Long getTicketId() {
        return this.ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }


    public String getEndUserMail() {
        return this.endUserMail;
    }

    public void setEndUserMail(String endUserMail) {
        this.endUserMail = endUserMail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTicketPk that = (UserTicketPk) o;
        return Objects.equals(this.ticketId, that.ticketId) &&
                Objects.equals(this.endUserMail, that.endUserMail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ticketId, this.endUserMail);
    }

    @Override
    public String toString() {
        return "UserTicketId{" +
                "ticketId=" + this.ticketId +
                ", endUserMail='" + this.endUserMail +
                "}";
    }
}
