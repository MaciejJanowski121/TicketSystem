package de.bachelorarbeit.ticketsystem.dto;

/**
 * DTO for error responses.
 */
public class ErrorResponse {

    private String message;

    // Default constructor
    public ErrorResponse() {
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}