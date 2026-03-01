package de.bachelorarbeit.ticketsystem.dto;

import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;

import java.time.Instant;

/**
 * DTO for ticket list item responses.
 */
public class TicketListItemResponse {

    private Long ticketId;
    private String title;
    private TicketState ticketState;
    private TicketCategory ticketCategory;
    private Instant createDate;
    private Instant updateDate;
    private Instant closedDate;
    private String creatorUsername;
    private String creatorEmail;
    private String assignedSupportUsername;

    // Default constructor
    public TicketListItemResponse() {
    }

    public TicketListItemResponse(Long ticketId, String title, TicketState ticketState, 
                                 TicketCategory ticketCategory, Instant createDate, Instant updateDate, 
                                 Instant closedDate, String creatorUsername, String creatorEmail, 
                                 String assignedSupportUsername) {
        this.ticketId = ticketId;
        this.title = title;
        this.ticketState = ticketState;
        this.ticketCategory = ticketCategory;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.closedDate = closedDate;
        this.creatorUsername = creatorUsername;
        this.creatorEmail = creatorEmail;
        this.assignedSupportUsername = assignedSupportUsername;
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

    public Instant getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Instant closedDate) {
        this.closedDate = closedDate;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getAssignedSupportUsername() {
        return assignedSupportUsername;
    }

    public void setAssignedSupportUsername(String assignedSupportUsername) {
        this.assignedSupportUsername = assignedSupportUsername;
    }
}