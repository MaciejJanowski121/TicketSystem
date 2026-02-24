package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.LoginRequest;
import de.bachelorarbeit.ticketsystem.dto.RegisterRequest;
import de.bachelorarbeit.ticketsystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * @return success message
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Register user (we don't need the token for registration response)
        authService.register(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());

        // Return success message
        Map<String, String> response = new HashMap<>();
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
}
