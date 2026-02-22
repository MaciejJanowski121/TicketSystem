package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Ticket entity representing a support ticket in the system.
 */
@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @Column(nullable = false)
    private Instant createDate;

    @Column(nullable = false)
    private Instant updateDate;

    private Instant closedDate;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketState ticketState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory ticketCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enduser_email", referencedColumnName = "mail", nullable = false)
    private UserAccount endUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_email", referencedColumnName = "mail")
    private UserAccount assignedSupport;

    // Default constructor required by JPA
    public Ticket() {
    }

    public Ticket(String title, String description, TicketCategory category, UserAccount endUser) {
        this.title = title;
        this.description = description;
        this.ticketState = TicketState.UNASSIGNED;
        this.ticketCategory = category;
        this.endUser = endUser;
        this.createDate = Instant.now();
        this.updateDate = Instant.now();
    }

    // Getters and setters

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Instant getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Instant createDate) {
        this.createDate = createDate;
    }

    public Instant getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Instant updateDate) {
        this.updateDate = updateDate;
    }

    public Instant getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Instant closedDate) {
        this.closedDate = closedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketState getTicketState() {
        return ticketState;
    }

    public void setTicketState(TicketState state) {
        this.ticketState = state;
    }

    public TicketCategory getTicketCategory() {
        return ticketCategory;
    }

    public void setTicketCategory(TicketCategory category) {
        this.ticketCategory = category;
    }

    public UserAccount getEndUser() {
        return endUser;
    }

    public void setEndUser(UserAccount endUser) {
        this.endUser = endUser;
    }

    public UserAccount getAssignedSupport() {
        return assignedSupport;
    }

    public void setAssignedSupport(UserAccount assignedSupport) {
        this.assignedSupport = assignedSupport;
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = Instant.now();
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId=" + ticketId +
                ", title='" + title + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", state=" + ticketState +
                ", category=" + ticketCategory +
                '}';
    }
}
