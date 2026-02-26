package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import de.bachelorarbeit.ticketsystem.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user.
     *
     * @param username the username
     * @param email the email
     * @param password the password
     * @return JWT token for the registered user
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public String register(String username, String email, String password) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByMail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user with ENDUSER role
        UserAccount user = new UserAccount(
                username,
                email,
                passwordEncoder.encode(password),
                Role.ENDUSER
        );

        // Save user
        userRepository.save(user);

        // Generate JWT token
        return jwtService.generateToken(user, email, Role.ENDUSER);
    }

    /**
     * Login a user.
     *
     * @param login the username or email
     * @param password the password
     * @return JWT token for the authenticated user
     * @throws AuthenticationException if authentication fails
     */
    public String login(String login, String password) {
        UserAccount user;

        // Check if login is an email
        if (login.contains("@")) {
            user = userRepository.findByMail(login)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + login));
        } else {
            user = userRepository.findByUsername(login)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + login));
        }

        // Authenticate user - use the original login parameter for authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, password)
        );

        // Generate JWT token
        return jwtService.generateToken(user, user.getMail(), user.getRole());
    }

    /**
     * Change password for an authenticated user.
     *
     * @param username the username of the authenticated user
     * @param currentPassword the current password
     * @param newPassword the new password
     * @param confirmPassword the password confirmation
     * @throws IllegalArgumentException if current password is invalid or passwords don't match
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        // Find user by username
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid current password");
        }

        // Validate password confirmation
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Optional: Check if new password is different from current password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Hash and save new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
