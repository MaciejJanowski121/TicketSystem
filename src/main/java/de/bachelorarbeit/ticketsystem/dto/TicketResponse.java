package de.bachelorarbeit.ticketsystem.dto;

import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;

import java.time.Instant;
import java.util.List;

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
    private String creatorUsername;
    private String creatorEmail;
    private Instant closedDate;
    private List<TicketCommentResponse> comments;

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

    public TicketResponse(Long ticketId, String title, String description, TicketState ticketState, 
                         TicketCategory ticketCategory, Instant createDate, Instant updateDate, 
                         String assignedSupport, String creatorUsername) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.ticketState = ticketState;
        this.ticketCategory = ticketCategory;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.assignedSupport = assignedSupport;
        this.creatorUsername = creatorUsername;
    }

    public TicketResponse(Long ticketId, String title, String description, TicketState ticketState, 
                         TicketCategory ticketCategory, Instant createDate, Instant updateDate, 
                         Instant closedDate, String assignedSupport, String creatorUsername, String creatorEmail) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.ticketState = ticketState;
        this.ticketCategory = ticketCategory;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.closedDate = closedDate;
        this.assignedSupport = assignedSupport;
        this.creatorUsername = creatorUsername;
        this.creatorEmail = creatorEmail;
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

    public Instant getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Instant closedDate) {
        this.closedDate = closedDate;
    }

    public List<TicketCommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<TicketCommentResponse> comments) {
        this.comments = comments;
    }
}
