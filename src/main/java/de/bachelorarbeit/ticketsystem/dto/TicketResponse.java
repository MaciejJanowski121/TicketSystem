package de.bachelorarbeit.ticketsystem.dto;

import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;

import java.time.Instant;

/**
 * DTO for ticket responses.
 */
public class TicketResponse {

    private Long ticketId;
    private String title;
    private String description;
    private TicketState ticketState;
    private TicketCategory ticketCategory;
    private Instant createDate;
    private Instant updateDate;
    private String assignedSupport;

    // Default constructor
    public TicketResponse() {
    }

    public TicketResponse(Long ticketId, String title, String description, TicketState ticketState, 
                         TicketCategory ticketCategory, Instant createDate, Instant updateDate, String assignedSupport) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.ticketState = ticketState;
        this.ticketCategory = ticketCategory;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.assignedSupport = assignedSupport;
    }

    // Getters and setters
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
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

    public void setTicketState(TicketState ticketState) {
        this.ticketState = ticketState;
    }

    public TicketCategory getTicketCategory() {
        return ticketCategory;
    }

    public void setTicketCategory(TicketCategory ticketCategory) {
        this.ticketCategory = ticketCategory;
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

    public String getAssignedSupport() {
        return assignedSupport;
    }

    public void setAssignedSupport(String assignedSupport) {
        this.assignedSupport = assignedSupport;
    }
}
