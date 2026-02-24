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

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), password)
        );

        // Generate JWT token
        return jwtService.generateToken(user, user.getMail(), user.getRole());
    }
}
