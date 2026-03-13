package de.bachelorarbeit.ticketsystem.exception;

import de.bachelorarbeit.ticketsystem.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("message", "Validierung fehlgeschlagen");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle authentication failures (login).
     */
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Ungültige Anmeldedaten"));
    }

    /**
     * Handle IllegalArgumentException from various operations.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage();

        // Check if it's from password change validation - let AuthController handle these
        if (message.equals("Invalid current password") || 
            message.equals("New password must be different from current password") ||
            message.equals("Passwords do not match")) {
            // Re-throw the exception so AuthController can handle it with proper German messages
            throw ex;
        }
        // Check if it's from register (user already exists)
        else if (message.contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Benutzer existiert bereits"));
        } 
        // Check if it's authentication required (missing authentication)
        else if (message.contains("Authentication is required")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Authentifizierung erforderlich"));
        }
        // Check if it's not found (user, ticket, etc.)
        else if (message.contains("not found")) {
            String germanMessage = message;
            if (message.contains("Ticket not found")) {
                germanMessage = "Ticket nicht gefunden";
            } else if (message.contains("User not found")) {
                germanMessage = "Benutzer nicht gefunden";
            } else if (message.contains("not found")) {
                germanMessage = "Nicht gefunden";
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(germanMessage));
        }
        // Check if it's access denied
        else if (message.contains("Access denied")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Zugriff verweigert"));
        }
        // Check if it's already assigned or closed (conflict scenarios)
        else if (message.contains("already assigned") || message.contains("closed")) {
            String germanMessage = message;
            if (message.contains("already assigned")) {
                germanMessage = "Ticket ist bereits zugewiesen";
            } else if (message.contains("closed")) {
                germanMessage = "Ticket ist bereits geschlossen";
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(germanMessage));
        }
        // Check if it's assignment required
        else if (message.contains("Assign ticket first")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Ticket muss zuerst zugewiesen werden"));
        }
        // Check if it's closing comment requirement
        else if (message.contains("Closing a ticket requires a concluding comment")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Das Schließen eines Tickets erfordert einen abschließenden Kommentar"));
        }
        // Check if it's support user access requirement
        else if (message.contains("Only support users can access")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Nur Support-Benutzer können darauf zugreifen"));
        }
        // For login/auth - user not found should be treated as invalid credentials
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Ungültige Anmeldedaten"));
        }
    }

    /**
     * Handle SecurityException from access control operations.
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Zugriff verweigert"));
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Ein Fehler ist bei der Verarbeitung der Anfrage aufgetreten"));
    }
}
