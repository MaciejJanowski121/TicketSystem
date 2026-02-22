package de.bachelorarbeit.ticketsystem.model.entity;

/**
 * Enum representing the possible states of a ticket in the system.
 */
public enum TicketState {
    UNASSIGNED("nicht zugeordnet"),
    IN_PROGRESS("in Bearbeitung"),
    CLOSED("abgeschlossen");

    private String stateDescription;

    TicketState(String stateDescription) {
        this.stateDescription = stateDescription;
    }

    public String getStateDescription() {
        return stateDescription;
    }
}