package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     *
     * @param requestBody the request body containing username, email, and password
     * @return JWT token for the registered user
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> requestBody) {
        try {
            String username = requestBody.get("username");
            String email = requestBody.get("email");
            String password = requestBody.get("password");

            // Validate input
            if (username == null || email == null || password == null) {
                return ResponseEntity.badRequest().body("Username, email, and password are required");
            }

            // Register user
            String token = authService.register(username, email, password);

            // Return token
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Login a user.
     * Accepts either request parameters or JSON body.
     *
     * @param login the username or email (from request parameter)
     * @param password the password (from request parameter)
     * @param requestBody the request body containing login and password (alternative to request parameters)
     * @return JWT token for the authenticated user
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam(required = false) String login,
            @RequestParam(required = false) String password,
            @RequestBody(required = false) Map<String, String> requestBody
    ) {
        try {
            // Get login and password from request parameters or request body
            String loginValue = login;
            String passwordValue = password;

            if (loginValue == null || passwordValue == null) {
                if (requestBody != null) {
                    loginValue = requestBody.get("login");
                    passwordValue = requestBody.get("password");
                }
            }

            // Validate input
            if (loginValue == null || passwordValue == null) {
                return ResponseEntity.badRequest().body("Login and password are required");
            }

            // Login user
            String token = authService.login(loginValue, passwordValue);

            // Return token
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }
}