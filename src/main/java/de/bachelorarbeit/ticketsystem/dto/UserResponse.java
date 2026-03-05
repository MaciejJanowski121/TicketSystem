package de.bachelorarbeit.ticketsystem.dto;

import de.bachelorarbeit.ticketsystem.model.entity.Role;

/**
 * DTO for user responses.
 */
public class UserResponse {

    private String mail;
    private String username;
    private Role role;

    // Default constructor
    public UserResponse() {
    }

    public UserResponse(String mail, String username, Role role) {
        this.mail = mail;
        this.username = username;
        this.role = role;
    }

    // Getters and setters
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}