package de.bachelorarbeit.ticketsystem.dto;

import de.bachelorarbeit.ticketsystem.model.entity.Role;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating user role requests.
 */
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    // Default constructor
    public UpdateUserRoleRequest() {
    }

    public UpdateUserRoleRequest(Role role) {
        this.role = role;
    }

    // Getters and setters
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}