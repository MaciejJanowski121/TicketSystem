package de.bachelorarbeit.ticketsystem.model.entity;

/**
 * Enum representing the possible categories of a ticket in the system.
 */
public enum TicketCategory {
    ACCOUNT_MANAGEMENT("Konto-Management"),
    HARDWARE("Hardware"),
    PROGRAMS_TOOLS("Programme und Tools"),
    NETWORK("Netzwerk"),
    OTHER("Sonstiges");

    private String categoryName;

    TicketCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }
}