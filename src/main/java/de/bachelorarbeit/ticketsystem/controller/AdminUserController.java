package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.ErrorResponse;
import de.bachelorarbeit.ticketsystem.dto.UpdateUserRoleRequest;
import de.bachelorarbeit.ticketsystem.dto.UserResponse;
import de.bachelorarbeit.ticketsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for admin user management operations.
 * Handles admin-specific user endpoints under /api/admin/users.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users in the system.
     * Optional endpoint that can be used by UI if needed.
     *
     * @param authentication the authentication object containing current user info
     * @return list of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINUSER')")
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Update a user's role.
     *
     * @param mail the email of the user to update
     * @param request the request containing the new role
     * @param authentication the authentication object containing current user info
     * @return the updated user
     */
    @PatchMapping("/{mail}/role")
    @PreAuthorize("hasRole('ADMINUSER')")
    public ResponseEntity<?> updateUserRole(@PathVariable String mail,
                                           @Valid @RequestBody UpdateUserRoleRequest request,
                                           Authentication authentication) {
        try {
            UserResponse updatedUser = userService.updateUserRole(mail, request.getRole());
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User not found"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(e.getMessage()));
            }
        }
    }
}
