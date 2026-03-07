package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.ChangePasswordRequest;
import de.bachelorarbeit.ticketsystem.dto.LoginRequest;
import de.bachelorarbeit.ticketsystem.dto.RegisterRequest;
import de.bachelorarbeit.ticketsystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     *
     * @param registerRequest the request body containing username, email, and password
     * @return JWT token for the registered user
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Register user and get JWT token
        String token = authService.register(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());

        // Return token for automatic login
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login a user.
     *
     * @param request the request body containing login and password
     * @return JWT token for the authenticated user
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        // Login user
        String token = authService.login(request.getLogin(), request.getPassword());

        // Return token
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    /**
     * Change password for authenticated user.
     *
     * @param changePasswordRequest the request body containing current and new passwords
     * @param bindingResult the validation result
     * @return success message
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            BindingResult bindingResult) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();

            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );

            response.put("message", "Validation failed");
            response.put("errors", errors);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Manual validation for password confirmation
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("confirmPassword", "Passwörter stimmen nicht überein");

            response.put("message", "Validierung fehlgeschlagen");
            response.put("errors", errors);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            // Change password
            authService.changePassword(
                    username,
                    changePasswordRequest.getCurrentPassword(),
                    changePasswordRequest.getNewPassword(),
                    changePasswordRequest.getConfirmPassword()
            );

            // Return success message
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Passwort erfolgreich geändert");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();

            // Handle specific error cases with German messages
            String errorMessage = e.getMessage();
            if ("Invalid current password".equals(errorMessage)) {
                errors.put("currentPassword", "Ungültiges aktuelles Passwort");
                response.put("message", "Ungültiges aktuelles Passwort");
            } else if ("New password must be different from current password".equals(errorMessage)) {
                errors.put("newPassword", "Das neue Passwort muss sich vom aktuellen Passwort unterscheiden");
                response.put("message", "Das neue Passwort muss sich vom aktuellen Passwort unterscheiden");
            } else if ("Passwords do not match".equals(errorMessage)) {
                errors.put("confirmPassword", "Passwörter stimmen nicht überein");
                response.put("message", "Passwörter stimmen nicht überein");
            } else {
                // Generic error message for other cases
                response.put("message", "Passwort-Änderung fehlgeschlagen: " + errorMessage);
            }

            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
