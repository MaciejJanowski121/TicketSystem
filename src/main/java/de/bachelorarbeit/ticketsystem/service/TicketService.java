package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for ticket operations.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new ticket.
     *
     * @param request the ticket creation request
     * @param authentication the authentication object containing current user info
     * @return the created ticket as TicketResponse
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, Authentication authentication) {
        // Determine current user identity from authentication.getName()
        String userIdentifier = authentication.getName();

        // Load UserAccount based on whether name contains '@' (email) or not (username)
        UserAccount endUser;
        if (userIdentifier.contains("@")) {
            endUser = userRepository.findByMail(userIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            endUser = userRepository.findByUsername(userIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        // Create Ticket using existing constructor
        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                request.getTicketCategory(),
                endUser
        );

        // Save ticket with TicketRepository
        Ticket savedTicket = ticketRepository.save(ticket);

        // Map saved entity to TicketResponse and return
        return new TicketResponse(
                savedTicket.getTicketId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getTicketState(),
                savedTicket.getTicketCategory(),
                savedTicket.getCreateDate(),
                savedTicket.getUpdateDate(),
                savedTicket.getAssignedSupport() != null ? savedTicket.getAssignedSupport().getUsername() : null
        );
    }

    /**
     * Get all tickets for the current authenticated user.
     *
     * @param authentication the authentication object containing current user info
     * @return list of tickets created by the current user, sorted by updateDate descending
     * @throws IllegalArgumentException if user not found or authentication is null
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(Authentication authentication) {
        // Defensive check for missing authentication
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is required");
        }

        UserAccount currentUser = getCurrentUser(authentication);

        // Get tickets sorted by updateDate descending using repository-level sorting
        List<Ticket> tickets = ticketRepository.findByEndUserOrderByUpdateDateDesc(currentUser);

        return tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific ticket by ID for the current authenticated user.
     *
     * @param ticketId the ID of the ticket to retrieve
     * @param authentication the authentication object containing current user info
     * @return the ticket details
     * @throws IllegalArgumentException if user not found or ticket not found or not owned by user
     */
    @Transactional(readOnly = true)
    public TicketResponse getMyTicketById(Long ticketId, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // Check if the ticket belongs to the current user
        if (!ticket.getEndUser().equals(currentUser)) {
            throw new IllegalArgumentException("Access denied: Ticket does not belong to current user");
        }

        return mapToTicketResponse(ticket);
    }

    /**
     * Get current user from authentication.
     *
     * @param authentication the authentication object
     * @return the current user
     * @throws IllegalArgumentException if user not found
     */
    private UserAccount getCurrentUser(Authentication authentication) {
        String userIdentifier = authentication.getName();

        if (userIdentifier.contains("@")) {
            return userRepository.findByMail(userIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            return userRepository.findByUsername(userIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }
    }

    /**
     * Map Ticket entity to TicketResponse DTO.
     *
     * @param ticket the ticket entity
     * @return the ticket response DTO
     */
    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getTicketState(),
                ticket.getTicketCategory(),
                ticket.getCreateDate(),
                ticket.getUpdateDate(),
                ticket.getAssignedSupport() != null ? ticket.getAssignedSupport().getUsername() : null
        );
    }
}
